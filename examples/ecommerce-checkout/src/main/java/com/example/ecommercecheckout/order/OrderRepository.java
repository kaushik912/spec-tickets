package com.example.ecommercecheckout.order;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    long countByCartId(Long cartId);
}
