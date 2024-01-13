package com.example.orderservices.repository;

import com.example.orderservices.model.OrderLineItems;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Meta;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderLineItemRepository extends JpaRepository<OrderLineItems, Long> {
    @Modifying
    @Meta(comment = "Delete order items by id")
    @Query("DELETE FROM OrderLineItems e WHERE e.id = :id")
    void deleteById(@Param("id") @NonNull Long id);
}