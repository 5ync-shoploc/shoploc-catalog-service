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

    @Test
    void getMyProducts_asCommercant_shouldReturnOnlyMerchantProducts() {
        ProductService service = mock(ProductService.class);
        ProductController controller = new ProductController(service);

        var pageable = PageRequest.of(0, 10);
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
        ProductService service = mock(ProductService.class);
        ProductController controller = new ProductController(service);

        var pageable = PageRequest.of(0, 10);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.getMyProducts("client-456", "CLIENT", pageable)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(service, never()).getMerchantProducts(any(), any());
    }

    @Test
    void updateProduct_asCommercantOwner_shouldReturnUpdatedProduct() {
        ProductService service = mock(ProductService.class);
        ProductController controller = new ProductController(service);

        var request = new com.shoploc.shoploccatalogservice.dto.UpdateProductRequest(
                "Baguette Modifiée", "Traditionnelle", BigDecimal.valueOf(1.40), 20
        );
        var updatedProduct = Product.builder()
                .id(1L)
                .name("Baguette Modifiée")
                .description("Traditionnelle")
                .price(BigDecimal.valueOf(1.40))
                .stock(20)
                .merchantId("merchant-123")
                .build();

        when(service.updateProduct(1L, request, "merchant-123")).thenReturn(updatedProduct);

        ResponseEntity<Product> response = controller.updateProduct(1L, "merchant-123", "COMMERCANT", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Baguette Modifiée", response.getBody().getName());
        assertEquals(20, response.getBody().getStock());
        verify(service).updateProduct(1L, request, "merchant-123");
    }

    @Test
    void updateProduct_asClient_shouldThrowForbidden() {
        ProductService service = mock(ProductService.class);
        ProductController controller = new ProductController(service);

        var request = new com.shoploc.shoploccatalogservice.dto.UpdateProductRequest(
                "Baguette", "Desc", BigDecimal.valueOf(1.40), 20
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.updateProduct(1L, "user-456", "CLIENT", request)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(service, never()).updateProduct(any(), any(), any());
    }

    @Test
    void deleteProduct_asCommercantOwner_shouldReturnNoContent() {
        ProductService service = mock(ProductService.class);
        ProductController controller = new ProductController(service);

        ResponseEntity<Void> response = controller.deleteProduct(1L, "merchant-123", "COMMERCANT");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service).deleteProduct(1L, "merchant-123");
    }

    @Test
    void deleteProduct_asClient_shouldThrowForbidden() {
        ProductService service = mock(ProductService.class);
        ProductController controller = new ProductController(service);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.deleteProduct(1L, "client-456", "CLIENT")
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(service, never()).deleteProduct(any(), any());
    }
}