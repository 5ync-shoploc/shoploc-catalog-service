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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final CommerceRepository commerceRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository repository,
                          CommerceRepository commerceRepository,
                          CategoryRepository categoryRepository) {
        this.repository = repository;
        this.commerceRepository = commerceRepository;
        this.categoryRepository = categoryRepository;
    }

    public Page<PublicProductResponse> getPublicCatalog(Pageable pageable) {
        return repository.findByActiveTrue(pageable)
                .map(PublicProductResponse::fromEntity);
    }

    public Page<Product> getMerchantProducts(String merchantId, Pageable pageable) {
        return repository.findByCommerce_IdCommercant(merchantId, pageable);
    }

    public Product createProduct(CreateProductRequest request, String merchantId) {
        Commerce commerce = commerceRepository.findById(request.commerceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commerce introuvable"));

        if (!commerce.getIdCommercant().equals(merchantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce commerce ne vous appartient pas");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Catégorie introuvable"));

        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .imageUrl(request.imageUrl())
                .alertThreshold(request.alertThreshold() != null ? request.alertThreshold() : 0)
                .commerce(commerce)
                .category(category)
                .active(true)
                .build();

        return repository.save(product);
    }

    public Product updateProduct(Long productId, UpdateProductRequest request, String merchantId) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article introuvable"));

        if (!product.getCommerce().getIdCommercant().equals(merchantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'êtes pas autorisé à modifier cet article");
        }

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setImageUrl(request.imageUrl());

        if (request.alertThreshold() != null) {
            product.setAlertThreshold(request.alertThreshold());
        }
        if (request.categoryId() != null) {
            product.setCategory(categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Catégorie introuvable")));
        }

        return repository.save(product);
    }

    public void deleteProduct(Long productId, String merchantId) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article introuvable"));

        if (!product.getCommerce().getIdCommercant().equals(merchantId)) {
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