package com.shoploc.shoploccatalogservice.repository;

import com.shoploc.shoploccatalogservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}