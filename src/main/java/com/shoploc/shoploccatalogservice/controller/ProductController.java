package com.shoploc.shoploccatalogservice.controller;

import com.shoploc.shoploccatalogservice.dto.CreateProductRequest;
import com.shoploc.shoploccatalogservice.entity.Product;
import com.shoploc.shoploccatalogservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public Page<Product> getAll(Pageable pageable) {
        return service.getAll(pageable);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Valid @RequestBody CreateProductRequest request
    ) {
        if (userId == null || userId.isBlank() || userRole == null || userRole.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentification requise");
        }

        if (!"COMMERCANT".equalsIgnoreCase(userRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul le commerçant peut créer un article");
        }

        Product created = service.createProduct(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}