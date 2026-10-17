package com.shoploc.shoploccatalogservice.repository;

import com.shoploc.shoploccatalogservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
