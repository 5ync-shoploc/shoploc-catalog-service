package com.shoploc.shoploccatalogservice.service;

import com.shoploc.shoploccatalogservice.dto.CreateProductRequest;
import com.shoploc.shoploccatalogservice.entity.Product;
import com.shoploc.shoploccatalogservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    @Test
    void getAll_shouldReturnPaginatedProducts() {
        var pageable = PageRequest.of(0, 10);
        var products = List.of(new Product(), new Product());
        Page<Product> page = new PageImpl<>(products, pageable, 2);

        when(repository.findAll(pageable)).thenReturn(page);

        Page<Product> result = service.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        verify(repository).findAll(pageable);
    }

    @Test
    void createProduct_shouldSaveAndReturnProduct() {
        var request = new CreateProductRequest("Pain", "Bio", BigDecimal.valueOf(2.50), 15);
        var savedProduct = Product.builder()
                .id(1L)
                .name("Pain")
                .description("Bio")
                .price(BigDecimal.valueOf(2.50))
                .stock(15)
                .merchantId("merchant-999")
                .build();

        when(repository.save(any(Product.class))).thenReturn(savedProduct);

        Product result = service.createProduct(request, "merchant-999");

        assertNotNull(result);
        assertEquals("Pain", result.getName());
        assertEquals("merchant-999", result.getMerchantId());
        verify(repository).save(any(Product.class));
    }

    @Test
    void deleteProduct_shouldSetProductToInactive() {
        var product = Product.builder()
                .id(1L)
                .name("Baguette")
                .merchantId("merchant-123")
                .active(true)
                .build();

        when(repository.findById(1L)).thenReturn(java.util.Optional.of(product));
        when(repository.save(any(Product.class))).thenReturn(product);

        service.deleteProduct(1L, "merchant-123");

        org.junit.jupiter.api.Assertions.assertFalse(product.getActive());
        verify(repository).save(product);
    }
}