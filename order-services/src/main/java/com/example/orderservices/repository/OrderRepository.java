package com.example.orderservices.repository;

import com.example.orderservices.model.OrderEntity;
import jakarta.annotation.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Meta;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Meta(comment = "find order by order items productId")
    List<OrderEntity> findOrderEntitiesByOrderLineItemsList_ProductId(Long productId);

    @Meta(comment = "find order by order items less than")
    List<OrderEntity> findOrderEntitiesByOrderLineItemsList_QuantityLessThanEqual(int quantity);

    @Meta(comment = "Sum quantity")
    @Query("select SUM(i.quantity) from OrderEntity o, OrderLineItems i where o.id = :id and o.id = i.order.id")
    @Nullable
    Integer sumOrderEntity_OrderLineItemsList_QuantityById(@Param("id") Long id);
}
