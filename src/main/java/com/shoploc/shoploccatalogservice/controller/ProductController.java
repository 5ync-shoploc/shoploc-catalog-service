package com.shoploc.shoploccatalogservice.controller;

import com.shoploc.shoploccatalogservice.entity.Product;
import com.shoploc.shoploccatalogservice.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public Page<Product> getAll(Pageable pageable) {
        return service.getAll(pageable);
    }
}
