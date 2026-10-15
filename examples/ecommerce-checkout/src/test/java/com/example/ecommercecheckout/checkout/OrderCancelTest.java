package com.example.ecommercecheckout.checkout;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
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
@TestPropertySource(properties = "payment.failure-rate=0.0")
class OrderCancelTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void cancelConfirmedOrderTriggersRefundAndMarksCancelled() throws Exception {
        Long orderId = checkoutConfirmedOrder();

        mockMvc.perform(post("/orders/{orderId}/cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        mockMvc.perform(get("/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    private Long checkoutConfirmedOrder() throws Exception {
        String cartResponse = mockMvc.perform(post("/carts"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long cartId = objectMapper.readTree(cartResponse).get("id").asLong();

        Map<String, Object> item = Map.of(
                "productName", "Widget",
                "unitPrice", 9.99,
                "quantity", 1
        );
        mockMvc.perform(post("/carts/{cartId}/items", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated());

        String checkoutResponse = mockMvc.perform(post("/carts/{cartId}/checkout", cartId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(checkoutResponse).get("id").asLong();
    }
}
