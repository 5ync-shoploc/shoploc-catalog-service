package com.shoploc.shoploccatalogservice.service;

import com.shoploc.shoploccatalogservice.dto.CreateProductRequest;
import com.shoploc.shoploccatalogservice.dto.PublicProductResponse;
import com.shoploc.shoploccatalogservice.dto.UpdateProductRequest;
import com.shoploc.shoploccatalogservice.entity.Product;
import com.shoploc.shoploccatalogservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;


    @Test
    void getPublicCatalog_shouldReturnMappedActiveProducts() {
        var pageable = PageRequest.of(0, 10);
        var inStock = Product.builder().id(1L).name("Baguette").description("Tradition")
                .price(BigDecimal.valueOf(1.20)).stock(5).merchantId("m1").build();
        var outOfStock = Product.builder().id(2L).name("Croissant").description("Beurre")
                .price(BigDecimal.valueOf(1.10)).stock(0).merchantId("m1").build();
        Page<Product> page = new PageImpl<>(List.of(inStock, outOfStock), pageable, 2);

        when(repository.findByActiveTrue(pageable)).thenReturn(page);

        Page<PublicProductResponse> result = service.getPublicCatalog(pageable);

        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().get(0).available());
        assertFalse(result.getContent().get(1).available());
        verify(repository).findByActiveTrue(pageable);
    }


    @Test
    void getMerchantProducts_shouldReturnProductsOfMerchant() {
        var pageable = PageRequest.of(0, 10);
        var products = List.of(new Product(), new Product());
        Page<Product> page = new PageImpl<>(products, pageable, 2);

        when(repository.findByMerchantId("merchant-123", pageable)).thenReturn(page);

        Page<Product> result = service.getMerchantProducts("merchant-123", pageable);

        assertEquals(2, result.getTotalElements());
        verify(repository).findByMerchantId("merchant-123", pageable);
    }


    @Test
    void createProduct_shouldSaveActiveProductWithMerchantId() {
        var request = new CreateProductRequest("Pain", "Bio", BigDecimal.valueOf(2.50), 15, "http://image.url/pain.jpg");
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = service.createProduct(request, "merchant-999");

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(repository).save(captor.capture());
        Product saved = captor.getValue();

        assertEquals("Pain", saved.getName());
        assertEquals("Bio", saved.getDescription());
        assertEquals(BigDecimal.valueOf(2.50), saved.getPrice());
        assertEquals(15, saved.getStock());
        assertEquals("http://image.url/pain.jpg", saved.getImageUrl());
        assertEquals("merchant-999", saved.getMerchantId());
        assertTrue(saved.getActive());
        assertEquals("merchant-999", result.getMerchantId());
    }


    @Test
    void updateProduct_asOwner_shouldUpdateFieldsAndSave() {
        var existing = Product.builder().id(1L).name("Old").description("Old desc")
                .price(BigDecimal.valueOf(1.00)).stock(1).merchantId("merchant-123").build();
        var request = new UpdateProductRequest("Baguette", "Desc", BigDecimal.valueOf(1.40), 20, "http://image.url/b.jpg");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = service.updateProduct(1L, request, "merchant-123");

        assertEquals("Baguette", result.getName());
        assertEquals("Desc", result.getDescription());
        assertEquals(BigDecimal.valueOf(1.40), result.getPrice());
        assertEquals(20, result.getStock());
        assertEquals("http://image.url/b.jpg", result.getImageUrl());
        verify(repository).save(existing);
    }

    @Test
    void updateProduct_notFound_shouldThrowNotFound() {
        var request = new UpdateProductRequest("Baguette", "Desc", BigDecimal.valueOf(1.40), 20, null);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.updateProduct(99L, request, "merchant-123"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void updateProduct_otherMerchant_shouldThrowForbidden() {
        var existing = Product.builder().id(1L).name("Baguette").merchantId("merchant-123").build();
        var request = new UpdateProductRequest("Baguette", "Desc", BigDecimal.valueOf(1.40), 20, null);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.updateProduct(1L, request, "merchant-OTHER"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(repository, never()).save(any());
    }


    @Test
    void deleteProduct_asOwner_shouldSetProductToInactive() {
        var product = Product.builder()
                .id(1L)
                .name("Baguette")
                .merchantId("merchant-123")
                .active(true)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(product));
        when(repository.save(any(Product.class))).thenReturn(product);

        service.deleteProduct(1L, "merchant-123");

        assertFalse(product.getActive());
        verify(repository).save(product);
    }

    @Test
    void deleteProduct_notFound_shouldThrowNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.deleteProduct(99L, "merchant-123"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void deleteProduct_otherMerchant_shouldThrowForbidden() {
        var product = Product.builder().id(1L).name("Baguette").merchantId("merchant-123").active(true).build();
        when(repository.findById(1L)).thenReturn(Optional.of(product));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.deleteProduct(1L, "merchant-OTHER"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertTrue(product.getActive());
        verify(repository, never()).save(any());
    }

    @Test
    void getProductDetail_shouldReturnAllProductInformation() {
        var product = Product.builder().id(1L).name("Baguette").description("Tradition")
                .price(BigDecimal.valueOf(1.20)).stock(5).imageUrl("http://image.url/b.jpg")
                .merchantId("m1").build();
        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));

        PublicProductResponse result = service.getProductDetail(1L);

        assertEquals(1L, result.id());
        assertEquals("Baguette", result.name());
        assertEquals("Tradition", result.description());
        assertEquals(BigDecimal.valueOf(1.20), result.price());
        assertEquals("http://image.url/b.jpg", result.imageUrl());
        assertTrue(result.available());
    }

    @Test
    void getProductDetail_outOfStock_shouldBeUnavailable() {
        var product = Product.builder().id(2L).name("Croissant").description("Beurre")
                .price(BigDecimal.valueOf(1.10)).stock(0).merchantId("m1").build();
        when(repository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(product));

        assertFalse(service.getProductDetail(2L).available());
    }

    @Test
    void getProductDetail_unknownOrInactiveId_shouldThrowNotFound() {
        when(repository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.getProductDetail(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}