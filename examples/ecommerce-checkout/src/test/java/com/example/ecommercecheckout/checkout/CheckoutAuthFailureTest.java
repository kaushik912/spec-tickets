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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "payment.failure-rate=1.0")
class CheckoutAuthFailureTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void authFailureLeavesNoOrderBehind() throws Exception {
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

        mockMvc.perform(post("/carts/{cartId}/checkout", cartId))
                .andExpect(status().isUnprocessableEntity());

        org.junit.jupiter.api.Assertions.assertEquals(0, orderRepository.countByCartId(cartId));
    }
}
