package com.example.ecommercecheckout.checkout;

import com.example.ecommercecheckout.order.OrderRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "payment.failure-rate=0.0")
class CheckoutIdempotencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void duplicateCheckoutWithSameKeyReturnsSameOrderWithoutSecondCharge() throws Exception {
        Long cartId = createCartWithOneItem();

        String first = mockMvc.perform(post("/carts/{cartId}/checkout", cartId)
                        .header("Idempotency-Key", "key-abc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long firstOrderId = objectMapper.readTree(first).get("id").asLong();

        String second = mockMvc.perform(post("/carts/{cartId}/checkout", cartId)
                        .header("Idempotency-Key", "key-abc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long secondOrderId = objectMapper.readTree(second).get("id").asLong();

        org.junit.jupiter.api.Assertions.assertEquals(firstOrderId, secondOrderId);
        org.junit.jupiter.api.Assertions.assertEquals(1, orderRepository.countByCartId(cartId));
    }

    @Test
    void checkoutWithoutIdempotencyKeyIsUnaffected() throws Exception {
        Long cartId = createCartWithOneItem();

        mockMvc.perform(post("/carts/{cartId}/checkout", cartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONFIRMED")));
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
