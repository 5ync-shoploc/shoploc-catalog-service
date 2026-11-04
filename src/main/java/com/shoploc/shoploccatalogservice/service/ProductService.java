package com.shoploc.shoploccatalogservice.service;

import com.shoploc.shoploccatalogservice.dto.CreateProductRequest;
import com.shoploc.shoploccatalogservice.entity.Product;
import com.shoploc.shoploccatalogservice.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public Page<Product> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Product createProduct(CreateProductRequest request, String merchantId) {
        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .merchantId(merchantId)
                .build();

        return repository.save(product);
    }

    public Page<Product> getMerchantProducts(String merchantId, Pageable pageable) {
        return repository.findByMerchantId(merchantId, pageable);
    }
}