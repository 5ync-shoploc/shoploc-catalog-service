package com.shoploc.shoploccatalogservice.controller;

import com.shoploc.shoploccatalogservice.dto.CreateProductRequest;
import com.shoploc.shoploccatalogservice.dto.PublicProductResponse;
import com.shoploc.shoploccatalogservice.dto.UpdateProductRequest;
import com.shoploc.shoploccatalogservice.entity.Product;
import com.shoploc.shoploccatalogservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductControllerTest {

    private ProductService service;
    private ProductController controller;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        service = mock(ProductService.class);
        controller = new ProductController(service);
        pageable = PageRequest.of(0, 10);
    }


    @Test
    void getPublicCatalog_shouldReturnActiveProductsPage() {
        var publicProduct = new PublicProductResponse(
                1L,
                "Baguette",
                "Baguette tradition",
                BigDecimal.valueOf(1.20),
                "http://image.url/baguette.jpg",
                true
        );

        Page<PublicProductResponse> expectedPage = new PageImpl<>(List.of(publicProduct), pageable, 1);
        when(service.getPublicCatalog(pageable)).thenReturn(expectedPage);

        Page<PublicProductResponse> result = controller.getPublicCatalog(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Baguette", result.getContent().get(0).name());
        assertTrue(result.getContent().get(0).available());
        verify(service).getPublicCatalog(pageable);
    }


    @Test
    void getMyProducts_asCommercant_shouldReturnOnlyMerchantProducts() {
        var product = Product.builder()
                .id(1L)
                .name("Baguette")
                .stock(30)
                .merchantId("merchant-123")
                .build();

        Page<Product> expectedPage = new PageImpl<>(List.of(product), pageable, 1);
        when(service.getMerchantProducts("merchant-123", pageable)).thenReturn(expectedPage);

        Page<Product> result = controller.getMyProducts("merchant-123", "COMMERCANT", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Baguette", result.getContent().get(0).getName());
        assertEquals(30, result.getContent().get(0).getStock());
        verify(service).getMerchantProducts("merchant-123", pageable);
    }

    @Test
    void getMyProducts_asClient_shouldThrowForbidden() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.getMyProducts("client-456", "CLIENT", pageable)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(service, never()).getMerchantProducts(any(), any());
    }

    @Test
    void getMyProducts_withoutHeaders_shouldThrowUnauthorized() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.getMyProducts(null, null, pageable)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verify(service, never()).getMerchantProducts(any(), any());
    }


    @Test
    void createProduct_asCommercant_shouldReturnCreated() {
        var request = new CreateProductRequest("Baguette", "Desc", BigDecimal.valueOf(1.20), 30, "http://image.url/baguette.jpg");
        var createdProduct = Product.builder()
                .id(1L)
                .name("Baguette")
                .price(BigDecimal.valueOf(1.20))
                .stock(30)
                .merchantId("merchant-123")
                .active(true)
                .build();

        when(service.createProduct(request, "merchant-123")).thenReturn(createdProduct);

        ResponseEntity<Product> response = controller.createProduct("merchant-123", "COMMERCANT", request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Baguette", response.getBody().getName());
        verify(service).createProduct(request, "merchant-123");
    }

    @Test
    void createProduct_asClient_shouldThrowForbidden() {
        var request = new CreateProductRequest("Baguette", "Desc", BigDecimal.valueOf(1.20), 30, "http://image.url/baguette.jpg");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.createProduct("client-456", "CLIENT", request)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(service, never()).createProduct(any(), any());
    }

    @Test
    void createProduct_withoutHeaders_shouldThrowUnauthorized() {
        var request = new CreateProductRequest("Baguette", "Desc", BigDecimal.valueOf(1.20), 30, "http://image.url/baguette.jpg");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.createProduct(null, null, request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verify(service, never()).createProduct(any(), any());
    }


    @Test
    void updateProduct_asCommercantOwner_shouldReturnUpdatedProduct() {
        var request = new UpdateProductRequest("Baguette", "Desc", BigDecimal.valueOf(1.40), 20, "http://image.url/baguette.jpg");
        var updatedProduct = Product.builder()
                .id(1L)
                .name("Baguette")
                .price(BigDecimal.valueOf(1.40))
                .stock(20)
                .merchantId("merchant-123")
                .build();

        when(service.updateProduct(1L, request, "merchant-123")).thenReturn(updatedProduct);

        ResponseEntity<Product> response = controller.updateProduct(1L, "merchant-123", "COMMERCANT", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(BigDecimal.valueOf(1.40), response.getBody().getPrice());
        verify(service).updateProduct(1L, request, "merchant-123");
    }

    @Test
    void updateProduct_asClient_shouldThrowForbidden() {
        var request = new UpdateProductRequest("Baguette", "Desc", BigDecimal.valueOf(1.40), 20, "http://image.url/baguette.jpg");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.updateProduct(1L, "client-456", "CLIENT", request)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(service, never()).updateProduct(any(), any(), any());
    }

    @Test
    void updateProduct_withoutHeaders_shouldThrowUnauthorized() {
        var request = new UpdateProductRequest("Baguette", "Desc", BigDecimal.valueOf(1.40), 20, "http://image.url/baguette.jpg");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.updateProduct(1L, null, null, request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verify(service, never()).updateProduct(any(), any(), any());
    }


    @Test
    void deleteProduct_asCommercantOwner_shouldReturnNoContent() {
        ResponseEntity<Void> response = controller.deleteProduct(1L, "merchant-123", "COMMERCANT");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service).deleteProduct(1L, "merchant-123");
    }

    @Test
    void deleteProduct_asClient_shouldThrowForbidden() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.deleteProduct(1L, "client-456", "CLIENT")
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(service, never()).deleteProduct(any(), any());
    }

    @Test
    void deleteProduct_withoutHeaders_shouldThrowUnauthorized() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.deleteProduct(1L, null, null)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verify(service, never()).deleteProduct(any(), any());
    }
}