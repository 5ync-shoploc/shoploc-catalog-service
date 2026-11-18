package com.shoploc.shoploccatalogservice.repository;

import com.shoploc.shoploccatalogservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByMerchantId(String merchantId, Pageable pageable);

    Page<Product> findByActiveTrue(Pageable pageable);

    Optional<Product> findByIdAndActiveTrue(Long id);
}
