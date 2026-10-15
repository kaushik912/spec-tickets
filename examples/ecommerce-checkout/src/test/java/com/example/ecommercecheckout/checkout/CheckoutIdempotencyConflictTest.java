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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "payment.failure-rate=0.0")
class CheckoutIdempotencyConflictTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void reusingKeyAgainstDifferentCartIsRejectedAsConflict() throws Exception {
        Long cartA = createCartWithOneItem();
        Long cartB = createCartWithOneItem();

        mockMvc.perform(post("/carts/{cartId}/checkout", cartA)
                        .header("Idempotency-Key", "shared-key"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/carts/{cartId}/checkout", cartB)
                        .header("Idempotency-Key", "shared-key"))
                .andExpect(status().isConflict());
    }

    private Long createCartWithOneItem() throws Exception {
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

        return cartId;
    }
}
