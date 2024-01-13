package com.example.productservices.controller;

import com.example.productservices.dto.ProductNameIdDto;
import com.example.productservices.dto.ProductRequestDto;
import com.example.productservices.dto.ProductResponseDto;
import com.example.productservices.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("")
    public ResponseEntity<List<ProductResponseDto>> getAllProduct() {
        List<ProductResponseDto> product = productService.getAllProduct();
        return ResponseEntity.status(HttpStatus.OK).body(product);
    }

    @PostMapping("")
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody ProductRequestDto productRequestDto) {
        ProductResponseDto product = productService.createProduct(productRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(@PathVariable Long id, @RequestBody ProductRequestDto productRequestDto) {
        if (productRequestDto.getName() == null || id == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        ProductResponseDto product = productService.updateProduct(id, productRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(product);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id){
        ProductResponseDto product = productService.getProductById(id);
        return ResponseEntity.status(HttpStatus.OK).body(product);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    @GetMapping("/get-by-name-id")
    public ResponseEntity<List<ProductResponseDto>> getByNameId(@RequestParam ProductNameIdDto productNameIdDto) {
        List<ProductResponseDto> list = productService.getByNameId(productNameIdDto);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @GetMapping("/get-total-price")
    public ResponseEntity<BigDecimal> getByNameId(@RequestParam(name = "order-id") Long orderId) {
        BigDecimal total = productService.getTotalPrice(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(total);
    }

    @GetMapping("/get-product/name/price")
    public ResponseEntity<List<ProductResponseDto>> getProductByNameAndPrice(@RequestParam String name, @RequestParam BigDecimal price) {
        List<ProductResponseDto> list = productService.getProductByNameAndPrice(name, price);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }
}
