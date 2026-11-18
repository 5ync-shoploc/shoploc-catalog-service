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
import com.shoploc.shoploccatalogservice.entity.Commerce;

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

    private static Commerce commerceOf(String idCommercant) {
        return Commerce.builder().id(10L).idCommercant(idCommercant).build();
    }

    @Test
    void getPublicCatalogShouldReturnActiveProductsPage() {
        var publicProduct = new PublicProductResponse(
                1L,
                "Baguette",
                "Baguette tradition",
                BigDecimal.valueOf(1.20),
                "http://image.url/baguette.jpg",
                true, 10L, 1L
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
    void getMyProductsAsCommercantShouldReturnOnlyMerchantProducts() {
        var product = Product.builder()
                .id(1L)
                .name("Baguette")
                .stock(30)
                .commerce(commerceOf("merchant-123"))
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
    void getMyProductsAsClientShouldThrowForbidden() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.getMyProducts("client-456", "CLIENT", pageable)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(service, never()).getMerchantProducts(any(), any());
    }

    @Test
    void getMyProductsWithoutHeadersShouldThrowUnauthorized() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.getMyProducts(null, null, pageable)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verify(service, never()).getMerchantProducts(any(), any());
    }

    @Test
    void createProductAsCommercantShouldReturnCreated() {
        var request = new CreateProductRequest("Baguette", "Desc", BigDecimal.valueOf(1.20), 30, "http://image.url/baguette.jpg", 5, 10L, 1L);
        var createdProduct = Product.builder()
                .id(1L)
                .name("Baguette")
                .price(BigDecimal.valueOf(1.20))
                .stock(30)
                .commerce(commerceOf("merchant-123"))
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
    void createProductAsClientShouldThrowForbidden() {
        var request = new CreateProductRequest("Baguette", "Desc", BigDecimal.valueOf(1.20), 30, "http://image.url/baguette.jpg", 5, 10L, 1L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.createProduct("client-456", "CLIENT", request)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(service, never()).createProduct(any(), any());
    }

    @Test
    void createProductWithoutHeadersShouldThrowUnauthorized() {
        var request = new CreateProductRequest("Baguette", "Desc", BigDecimal.valueOf(1.20), 30, "http://image.url/baguette.jpg", 5, 10L, 1L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.createProduct(null, null, request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verify(service, never()).createProduct(any(), any());
    }

    @Test
    void updateProductAsCommercantOwnerShouldReturnUpdatedProduct() {
        var request = new UpdateProductRequest("Baguette", "Desc", BigDecimal.valueOf(1.40), 20, "http://image.url/baguette.jpg", 5, null);
        var updatedProduct = Product.builder()
                .id(1L)
                .name("Baguette")
                .price(BigDecimal.valueOf(1.40))
                .stock(20)
                .commerce(commerceOf("merchant-123"))
                .build();

        when(service.updateProduct(1L, request, "merchant-123")).thenReturn(updatedProduct);

        ResponseEntity<Product> response = controller.updateProduct(1L, "merchant-123", "COMMERCANT", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(BigDecimal.valueOf(1.40), response.getBody().getPrice());
        verify(service).updateProduct(1L, request, "merchant-123");
    }

    @Test
    void updateProductAsClientShouldThrowForbidden() {
        var request = new UpdateProductRequest("Baguette", "Desc", BigDecimal.valueOf(1.40), 20, "http://image.url/baguette.jpg", 5, null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.updateProduct(1L, "client-456", "CLIENT", request)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(service, never()).updateProduct(any(), any(), any());
    }

    @Test
    void updateProductWithoutHeadersShouldThrowUnauthorized() {
        var request = new UpdateProductRequest("Baguette", "Desc", BigDecimal.valueOf(1.40), 20, "http://image.url/baguette.jpg", 5, null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.updateProduct(1L, null, null, request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verify(service, never()).updateProduct(any(), any(), any());
    }

    @Test
    void deleteProductAsCommercantOwnerShouldReturnNoContent() {
        ResponseEntity<Void> response = controller.deleteProduct(1L, "merchant-123", "COMMERCANT");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service).deleteProduct(1L, "merchant-123");
    }

    @Test
    void deleteProductAsClientShouldThrowForbidden() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.deleteProduct(1L, "client-456", "CLIENT")
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(service, never()).deleteProduct(any(), any());
    }

    @Test
    void deleteProductWithoutHeadersShouldThrowUnauthorized() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.deleteProduct(1L, null, null)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verify(service, never()).deleteProduct(any(), any());
    }

    @Test
    void getProductDetailShouldReturnProduct() {
        var response = new PublicProductResponse(
                1L, "Baguette", "Baguette tradition", BigDecimal.valueOf(1.20),
                "http://image.url/baguette.jpg", true, 10L, 1L);
        when(service.getProductDetail(1L)).thenReturn(response);

        PublicProductResponse result = controller.getProductDetail(1L);

        assertEquals(1L, result.id());
        assertEquals("Baguette", result.name());
        assertEquals("Baguette tradition", result.description());
        assertEquals(BigDecimal.valueOf(1.20), result.price());
        assertEquals("http://image.url/baguette.jpg", result.imageUrl());
        assertTrue(result.available());
        verify(service).getProductDetail(1L);
    }

    @Test
    void getProductDetailUnknownIdShouldPropagateNotFound() {
        when(service.getProductDetail(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Article introuvable"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.getProductDetail(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}