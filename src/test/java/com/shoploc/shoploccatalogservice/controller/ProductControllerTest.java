package com.shoploc.shoploccatalogservice.controller;

import com.shoploc.shoploccatalogservice.dto.CreateProductRequest;
import com.shoploc.shoploccatalogservice.entity.Product;
import com.shoploc.shoploccatalogservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProductControllerTest {

    @Test
    void getAll_shouldReturnPaginatedProducts() {
        ProductService service = mock(ProductService.class);
        ProductController controller = new ProductController(service);

        var pageable = PageRequest.of(0, 10);
        var product = new Product();
        product.setName("Laptop");
        product.setPrice(BigDecimal.valueOf(879.99));

        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);

        when(service.getAll(pageable)).thenReturn(page);

        Page<Product> result = controller.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Laptop", result.getContent().get(0).getName());
        verify(service).getAll(pageable);
    }

    @Test
    void createProduct_asCommercant_shouldReturnCreatedProduct() {
        ProductService service = mock(ProductService.class);
        ProductController controller = new ProductController(service);

        var request = new CreateProductRequest("Baguette", "Tradition", BigDecimal.valueOf(1.20), 30);
        var expectedProduct = Product.builder()
                .id(1L)
                .name("Baguette")
                .description("Tradition")
                .price(BigDecimal.valueOf(1.20))
                .stock(30)
                .merchantId("merchant-123")
                .build();

        when(service.createProduct(request, "merchant-123")).thenReturn(expectedProduct);

        ResponseEntity<Product> response = controller.createProduct("merchant-123", "COMMERCANT", request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Baguette", response.getBody().getName());
        assertEquals("merchant-123", response.getBody().getMerchantId());
        verify(service).createProduct(request, "merchant-123");
    }

    @Test
    void createProduct_asClient_shouldThrowForbidden() {
        ProductService service = mock(ProductService.class);
        ProductController controller = new ProductController(service);

        var request = new CreateProductRequest("Baguette", "Tradition", BigDecimal.valueOf(1.20), 30);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.createProduct("user-456", "CLIENT", request)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(service, never()).createProduct(any(), any());
    }

    @Test
    void createProduct_withoutHeaders_shouldThrowUnauthorized() {
        ProductService service = mock(ProductService.class);
        ProductController controller = new ProductController(service);

        var request = new CreateProductRequest("Baguette", "Tradition", BigDecimal.valueOf(1.20), 30);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.createProduct(null, null, request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verify(service, never()).createProduct(any(), any());
    }
}