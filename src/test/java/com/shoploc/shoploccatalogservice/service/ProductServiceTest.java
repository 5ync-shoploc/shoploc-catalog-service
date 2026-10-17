package com.shoploc.shoploccatalogservice.service;

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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        var products = List.of(
                new Product(),
                new Product()
        );

        Page<Product> page = new PageImpl<>(products, pageable, 2);

        when(repository.findAll(pageable)).thenReturn(page);

        Page<Product> result = service.getAll(pageable);

        assertEquals(2, result.getTotalElements());
        verify(repository).findAll(pageable);
    }
}