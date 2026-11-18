package com.shoploc.shoploccatalogservice.service;

import com.shoploc.shoploccatalogservice.dto.CreateProductRequest;
import com.shoploc.shoploccatalogservice.dto.PublicProductResponse;
import com.shoploc.shoploccatalogservice.dto.UpdateProductRequest;
import com.shoploc.shoploccatalogservice.entity.Product;
import com.shoploc.shoploccatalogservice.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public Page<PublicProductResponse> getPublicCatalog(Pageable pageable) {
        return repository.findByActiveTrue(pageable)
                .map(PublicProductResponse::fromEntity);
    }

    public Page<Product> getMerchantProducts(String merchantId, Pageable pageable) {
        return repository.findByMerchantId(merchantId, pageable);
    }

    public Product createProduct(CreateProductRequest request, String merchantId) {
        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .imageUrl(request.imageUrl())
                .merchantId(merchantId)
                .active(true)
                .build();

        return repository.save(product);
    }

    public Product updateProduct(Long productId, UpdateProductRequest request, String merchantId) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article introuvable"));

        if (!product.getMerchantId().equals(merchantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'êtes pas autorisé à modifier cet article");
        }

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setImageUrl(request.imageUrl());

        return repository.save(product);
    }

    public void deleteProduct(Long productId, String merchantId) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article introuvable"));

        if (!product.getMerchantId().equals(merchantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'êtes pas autorisé à supprimer cet article");
        }

        product.setActive(false);
        repository.save(product);
    }

    public PublicProductResponse getProductDetail(Long productId) {
        return repository.findByIdAndActiveTrue(productId)
                .map(PublicProductResponse::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article introuvable"));
    }
}