package com.shoploc.shoploccatalogservice.controller;

import com.shoploc.shoploccatalogservice.entity.Product;
import com.shoploc.shoploccatalogservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProductControllerTest {

    @Test
    void getAll_shouldReturnPaginatedProducts() {
        ProductService service = mock(ProductService.class);
        ProductController controller = new ProductController(service);

        var pageable = PageRequest.of(0, 10);
        var product = new Product();
        product.setName("Laptop");
        product.setPrice(899.99);

        Page<Product> page =
                new PageImpl<>(List.of(product), pageable, 1);

        when(service.getAll(pageable)).thenReturn(page);

        Page<Product> result = controller.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Laptop", result.getContent().get(0).getName());
        verify(service).getAll(pageable);
    }
}