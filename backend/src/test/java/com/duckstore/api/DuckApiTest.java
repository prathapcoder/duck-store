package com.duckstore.api;

import static com.duckstore.testsupport.DuckJson.aDuck;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.duckstore.entity.Duck;
import com.duckstore.repository.DuckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** The warehouse API end to end: controller, service, repository and the embedded database. */
@SpringBootTest
@AutoConfigureMockMvc
class DuckApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private DuckRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private ResultActions addDuck(String color, String size, String price, int quantity) throws Exception {
        return mvc.perform(post("/api/ducks").contentType(MediaType.APPLICATION_JSON).content(
                aDuck().color(color).size(size).price(price).quantity(quantity).build()));
    }

    @Test
    void adding_the_same_color_size_and_price_merges_the_quantities() throws Exception {
        addDuck("Red", "Large", "12.50", 10).andExpect(status().isCreated()).andExpect(jsonPath("$.quantity").value(10));
        addDuck("red", "LARGE", "12.5", 5).andExpect(status().isOk()).andExpect(jsonPath("$.quantity").value(15));

        mvc.perform(get("/api/ducks")).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void a_different_price_is_a_separate_record() throws Exception {
        addDuck("Red", "Large", "12.50", 10);
        addDuck("Red", "Large", "13.00", 10).andExpect(status().isCreated());

        mvc.perform(get("/api/ducks")).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void the_listing_is_sorted_by_quantity_descending_by_default() throws Exception {
        addDuck("Red", "Large", "1.00", 5);
        addDuck("Black", "Large", "1.00", 50);
        addDuck("Green", "Large", "1.00", 20);

        mvc.perform(get("/api/ducks"))
                .andExpect(jsonPath("$[0].quantity").value(50))
                .andExpect(jsonPath("$[1].quantity").value(20))
                .andExpect(jsonPath("$[2].quantity").value(5));
        mvc.perform(get("/api/ducks?sort=quantity&dir=asc")).andExpect(jsonPath("$[0].quantity").value(5));
    }

    @Test
    void sizes_sort_by_magnitude_not_alphabetically() throws Exception {
        addDuck("Red", "Small", "1.00", 1);
        addDuck("Red", "XLarge", "1.00", 1);
        addDuck("Red", "Medium", "1.00", 1);

        mvc.perform(get("/api/ducks?sort=size&dir=desc"))
                .andExpect(jsonPath("$[0].size").value("XLarge"))
                .andExpect(jsonPath("$[1].size").value("Medium"))
                .andExpect(jsonPath("$[2].size").value("Small"));
    }

    @Test
    void an_unknown_sort_field_is_a_400() throws Exception {
        mvc.perform(get("/api/ducks?sort=weight")).andExpect(status().isBadRequest());
    }

    @Test
    void editing_changes_price_and_quantity() throws Exception {
        addDuck("Red", "Large", "10.00", 10);
        int id = repository.findAll().get(0).getId();

        mvc.perform(put("/api/ducks/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"price\":11.25,\"quantity\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(11.25))
                .andExpect(jsonPath("$.quantity").value(0));
    }

    @Test
    void color_and_size_are_read_only_but_may_be_echoed_back_unchanged() throws Exception {
        addDuck("Red", "Large", "10.00", 10);
        int id = repository.findAll().get(0).getId();

        mvc.perform(put("/api/ducks/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"color\":\"Red\",\"size\":\"Large\",\"price\":10,\"quantity\":3}"))
                .andExpect(status().isOk());
        mvc.perform(put("/api/ducks/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"color\":\"Black\",\"price\":10,\"quantity\":3}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("color cannot be changed")));
    }

    @Test
    void editing_a_price_onto_another_record_is_a_409() throws Exception {
        addDuck("Red", "Large", "10.00", 1);
        addDuck("Red", "Large", "11.00", 1);
        int id = repository.findAll().stream().min(java.util.Comparator.comparing(Duck::getPrice)).orElseThrow().getId();

        mvc.perform(put("/api/ducks/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"price\":11.00,\"quantity\":1}"))
                .andExpect(status().isConflict());
    }

    @Test
    void delete_is_logical_and_the_duck_disappears() throws Exception {
        addDuck("Red", "Large", "10.00", 1);
        int id = repository.findAll().get(0).getId();

        mvc.perform(delete("/api/ducks/" + id)).andExpect(status().isNoContent());

        mvc.perform(get("/api/ducks")).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/ducks/" + id)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/ducks/" + id)).andExpect(status().isNotFound());
        org.assertj.core.api.Assertions.assertThat(repository.count()).isEqualTo(1);   // the row is kept
    }

    @Test
    void adding_a_deleted_duck_again_creates_a_new_record() throws Exception {
        addDuck("Red", "Large", "10.00", 4);
        int id = repository.findAll().get(0).getId();
        mvc.perform(delete("/api/ducks/" + id));

        addDuck("Red", "Large", "10.00", 6).andExpect(status().isCreated()).andExpect(jsonPath("$.quantity").value(6));
    }

    @Test
    void invalid_input_is_reported_per_field() throws Exception {
        addDuck("Red", "Large", "10.505", 0)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.quantity").exists());
        addDuck("Purple", "Large", "10.00", 1)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("Allowed values: Red, Green, Yellow, Black")));
        mvc.perform(post("/api/ducks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"color\":\"Red\",\"size\":\"Large\",\"price\":1,\"quantity\":1.5}"))
                .andExpect(status().isBadRequest());
    }
}
