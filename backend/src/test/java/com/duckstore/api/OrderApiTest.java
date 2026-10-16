package com.duckstore.api;

import static com.duckstore.testsupport.OrderJson.anOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.duckstore.common.Color;
import com.duckstore.common.Size;
import com.duckstore.repository.DuckRepository;
import com.duckstore.service.DuckService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class OrderApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private DuckService ducks;

    @Autowired
    private DuckRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private ResultActions order(String color, String size, int quantity, String country, String mode) throws Exception {
        return mvc.perform(post("/api/store/orders").contentType(MediaType.APPLICATION_JSON).content(
                anOrder().color(color).size(size).quantity(quantity).country(country).mode(mode).build()));
    }

    @Test
    void prices_an_order_from_warehouse_data_and_returns_packaging_and_the_breakdown() throws Exception {
        ducks.add(Color.RED, Size.XLARGE, new BigDecimal("200.00"), 10000);

        order("Red", "XLarge", 10, "USA", "Air")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.packageType").value("Wood"))
                .andExpect(jsonPath("$.protections[0]").value("Polystyrene balls"))
                .andExpect(jsonPath("$.unitPrice").value(200.00))
                .andExpect(jsonPath("$.subtotal").value(2000.00))
                .andExpect(jsonPath("$.adjustments.length()").value(3))
                .andExpect(jsonPath("$.adjustments[1].code").value("DESTINATION_SURCHARGE"))
                .andExpect(jsonPath("$.adjustments[1].type").value("INCREMENT"))
                .andExpect(jsonPath("$.adjustments[1].amount").value(378.00))
                .andExpect(jsonPath("$.total").value(2778.00));
    }

    @Test
    void with_several_prices_the_cheapest_lot_in_stock_is_used() throws Exception {
        ducks.add(Color.RED, Size.SMALL, new BigDecimal("3.00"), 1);
        ducks.add(Color.RED, Size.SMALL, new BigDecimal("5.00"), 100);
        ducks.add(Color.RED, Size.SMALL, new BigDecimal("4.00"), 100);

        order("Red", "Small", 1, "France", "Land").andExpect(jsonPath("$.unitPrice").value(3.00));
    }

    @Test
    void an_unavailable_duck_is_a_404() throws Exception {
        order("Black", "Medium", 1, "India", "Sea").andExpect(status().isNotFound());
    }

    @Test
    void invalid_orders_are_400s() throws Exception {
        order("Red", "XLarge", 0, "USA", "Air").andExpect(status().isBadRequest());
        order("Red", "XLarge", 1, " ", "Air").andExpect(status().isBadRequest());
        order("Red", "XLarge", 1, "USA", "Rocket").andExpect(status().isBadRequest());
    }
}
