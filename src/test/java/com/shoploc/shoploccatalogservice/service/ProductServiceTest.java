
package com.shoploc.shoploccatalogservice.service;

import com.shoploc.shoploccatalogservice.dto.CreateProductRequest;
import com.shoploc.shoploccatalogservice.dto.PublicProductResponse;
import com.shoploc.shoploccatalogservice.dto.UpdateProductRequest;
import com.shoploc.shoploccatalogservice.entity.Category;
import com.shoploc.shoploccatalogservice.entity.Commerce;
import com.shoploc.shoploccatalogservice.entity.Product;
import com.shoploc.shoploccatalogservice.repository.CategoryRepository;
import com.shoploc.shoploccatalogservice.repository.CommerceRepository;
import com.shoploc.shoploccatalogservice.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Mock
    private CommerceRepository commerceRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Commerce commerce;
    private Category category;
    private Product product;

    private final String merchantId = "merchant-123";

    @BeforeEach
    void setUp() {

        commerce = Commerce.builder()
                .id(1L)
                .name("Commerce Test")
                .idCommercant(merchantId)
                .build();

        category = Category.builder()
                .id(1L)
                .label("Alimentation")
                .build();

        product = Product.builder()
                .id(1L)
                .name("Riz")
                .description("Riz blanc")
                .price(new BigDecimal("5.00"))
                .stock(20)
                .alertThreshold(5)
                .imageUrl("riz.jpg")
                .commerce(commerce)
                .category(category)
                .active(true)
                .build();
    }

    @Test
    void testGetPublicCatalog() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> products = new PageImpl<>(List.of(product));

        when(repository.findByActiveTrue(pageable))
                .thenReturn(products);

        Page<PublicProductResponse> result =
                productService.getPublicCatalog(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Riz", result.getContent().get(0).name());
        assertTrue(result.getContent().get(0).available());

        verify(repository).findByActiveTrue(pageable);
    }


    @Test
    void testGetMerchantProducts() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> products = new PageImpl<>(List.of(product));

        when(repository.findByCommerce_IdCommercant(merchantId, pageable))
                .thenReturn(products);

        Page<Product> result =
                productService.getMerchantProducts(merchantId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Riz", result.getContent().get(0).getName());

        verify(repository).findByCommerce_IdCommercant(merchantId, pageable);
    }


    @Test
    void testCreateProduct() {

        CreateProductRequest request = new CreateProductRequest(
                "Riz",
                "Riz blanc",
                new BigDecimal("5.00"),
                20,
                "riz.jpg",
                5,
                1L,
                1L
        );

        when(commerceRepository.findById(1L))
                .thenReturn(Optional.of(commerce));

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(repository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.createProduct(request, merchantId);

        assertNotNull(result);
        assertEquals("Riz", result.getName());
        assertEquals(new BigDecimal("5.00"), result.getPrice());
        assertEquals(20, result.getStock());
        assertEquals(5, result.getAlertThreshold());
        assertTrue(result.getActive());

        verify(repository).save(any(Product.class));
    }


    @Test
    void testCreateProductCommerceNotFound() {

        CreateProductRequest request = new CreateProductRequest(
                "Riz", "Riz blanc",
                new BigDecimal("5.00"), 20,
                null, null, 1L, 1L
        );

        when(commerceRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productService.createProduct(request, merchantId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(repository, never()).save(any());
    }

    @Test
    void testCreateProductForbidden() {

        CreateProductRequest request = new CreateProductRequest(
                "Riz", "Riz blanc",
                new BigDecimal("5.00"), 20,
                null, null, 1L, 1L
        );

        when(commerceRepository.findById(1L))
                .thenReturn(Optional.of(commerce));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productService.createProduct(request, "autre-merchant")
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

        verify(categoryRepository, never()).findById(any());
        verify(repository, never()).save(any());
    }

    @Test
    void testCreateProductCategoryNotFound() {

        CreateProductRequest request = new CreateProductRequest(
                "Riz", "Riz blanc",
                new BigDecimal("5.00"), 20,
                null, null, 1L, 2L
        );

        when(commerceRepository.findById(1L))
                .thenReturn(Optional.of(commerce));

        when(categoryRepository.findById(2L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productService.createProduct(request, merchantId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(repository, never()).save(any());
    }


    @Test
    void testUpdateProduct() {

        UpdateProductRequest request = new UpdateProductRequest(
                "Riz premium",
                "Riz de qualité supérieure",
                new BigDecimal("8.00"),
                30,
                "riz-premium.jpg",
                10,
                1L
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(product));

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(repository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product result =
                productService.updateProduct(1L, request, merchantId);

        assertEquals("Riz premium", result.getName());
        assertEquals("Riz de qualité supérieure", result.getDescription());
        assertEquals(new BigDecimal("8.00"), result.getPrice());
        assertEquals(30, result.getStock());
        assertEquals(10, result.getAlertThreshold());

        verify(repository).save(product);
    }


    @Test
    void testUpdateProductNotFound() {

        UpdateProductRequest request = new UpdateProductRequest(
                "Riz", "Riz blanc",
                new BigDecimal("5.00"), 20,
                null, null, null
        );

        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productService.updateProduct(99L, request, merchantId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(repository, never()).save(any());
    }


    @Test
    void testUpdateProductForbidden() {

        UpdateProductRequest request = new UpdateProductRequest(
                "Riz", "Riz blanc",
                new BigDecimal("5.00"), 20,
                null, null, null
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(product));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productService.updateProduct(1L, request, "autre-merchant")
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

        verify(repository, never()).save(any());
    }

    @Test
    void testDeleteProduct() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(product));

        productService.deleteProduct(1L, merchantId);

        assertFalse(product.getActive());

        verify(repository).save(product);
    }

    @Test
    void testDeleteProductNotFound() {

        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productService.deleteProduct(99L, merchantId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(repository, never()).save(any());
    }


    @Test
    void testDeleteProductForbidden() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(product));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productService.deleteProduct(1L, "autre-merchant")
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

        verify(repository, never()).save(any());
    }


    @Test
    void testGetProductDetail() {

        when(repository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(product));

        PublicProductResponse result =
                productService.getProductDetail(1L);

        assertNotNull(result);
        assertEquals("Riz", result.name());
        assertEquals(new BigDecimal("5.00"), result.price());
        assertTrue(result.available());
    }

    @Test
    void testGetProductDetailNotFound() {

        when(repository.findByIdAndActiveTrue(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productService.getProductDetail(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
