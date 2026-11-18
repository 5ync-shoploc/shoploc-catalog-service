package com.shoploc.shoploccatalogservice.controller;

import com.shoploc.shoploccatalogservice.dto.CreateProductRequest;
import com.shoploc.shoploccatalogservice.dto.PublicProductResponse;
import com.shoploc.shoploccatalogservice.dto.UpdateProductRequest;
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

    private static final String ROLE_COMMERCANT = "COMMERCANT";

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public Page<PublicProductResponse> getPublicCatalog(Pageable pageable) {
        return service.getPublicCatalog(pageable);
    }

    @GetMapping("/my-products")
    public Page<Product> getMyProducts(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            Pageable pageable
    ) {
        requireCommercant(userId, userRole, "Seul le commerçant peut consulter ses articles");
        return service.getMerchantProducts(userId, pageable);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Valid @RequestBody CreateProductRequest request
    ) {
        requireCommercant(userId, userRole, "Seul le commerçant peut créer un article");
        Product created = service.createProduct(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        requireCommercant(userId, userRole, "Seul le commerçant peut modifier un article");
        Product updated = service.updateProduct(id, request, userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireCommercant(userId, userRole, "Seul le commerçant peut supprimer un article");
        service.deleteProduct(id, userId);
        return ResponseEntity.noContent().build();
    }

    private void requireCommercant(String userId, String userRole, String forbiddenMessage) {
        if (userId == null || userId.isBlank() || userRole == null || userRole.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentification requise");
        }
        if (!ROLE_COMMERCANT.equalsIgnoreCase(userRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, forbiddenMessage);
        }
    }

    @GetMapping("/{id}")
    public PublicProductResponse getProductDetail(@PathVariable Long id) {
        return service.getProductDetail(id);
    }
}