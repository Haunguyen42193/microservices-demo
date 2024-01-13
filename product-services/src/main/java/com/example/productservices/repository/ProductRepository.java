package com.example.productservices.repository;

import com.example.productservices.model.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Meta;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    @Meta(comment = "find product by name")
    List<ProductEntity> findProductByName(String name);

    @Meta(comment = "find product by name and price")
    List<ProductEntity> findProductEntitiesByNameAndPrice(String name, BigDecimal price);

    @Meta(comment = "total price by id")
    @Query("select p.price * :quantity from #{#entityName} p where p.id = :id")
    BigDecimal sumProductPriceByIdAndQuantity(@Param("id") Long productId, @Param("quantity") int quantity);

    @Meta(comment = "find product by name and price order by price")
    List<ProductEntity> findProductEntitiesByNameLikeAndPriceOrderByPrice(String name, BigDecimal price);
}
