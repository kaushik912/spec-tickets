package com.example.ecommercecheckout.cart;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CartApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createCartAddItemAndFetchIt() throws Exception {
        String cartResponse = mockMvc.perform(post("/carts"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long cartId = objectMapper.readTree(cartResponse).get("id").asLong();

        Map<String, Object> item = Map.of(
                "productName", "Widget",
                "unitPrice", 9.99,
                "quantity", 3
        );

        mockMvc.perform(post("/carts/{cartId}/items", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/carts/{cartId}", cartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(cartId.intValue())))
                .andExpect(jsonPath("$.items.length()", is(1)))
                .andExpect(jsonPath("$.items[0].productName", is("Widget")))
                .andExpect(jsonPath("$.items[0].quantity", is(3)));
    }

    @Test
    void rejectsInvalidItem() throws Exception {
        String cartResponse = mockMvc.perform(post("/carts"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long cartId = objectMapper.readTree(cartResponse).get("id").asLong();

        Map<String, Object> invalidItem = Map.of(
                "productName", "",
                "unitPrice", -1,
                "quantity", 0
        );

        mockMvc.perform(post("/carts/{cartId}/items", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItem)))
                .andExpect(status().isBadRequest());
    }
}
