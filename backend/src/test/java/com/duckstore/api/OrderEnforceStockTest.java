package com.duckstore.api;

import static com.duckstore.testsupport.OrderJson.anOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

/** The same endpoint with duckstore.store.enforce-stock switched on. */
@SpringBootTest(properties = "duckstore.store.enforce-stock=true")
@AutoConfigureMockMvc
class OrderEnforceStockTest {

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

    @Test
    void an_order_larger_than_any_single_lot_is_a_409() throws Exception {
        ducks.add(Color.RED, Size.LARGE, new BigDecimal("9.00"), 5);

        mvc.perform(post("/api/store/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(anOrder().color("Red").size("Large").quantity(6).country("USA").mode("Sea").build()))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/store/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(anOrder().color("Red").size("Large").quantity(5).country("USA").mode("Sea").build()))
                .andExpect(status().isOk());
    }
}
