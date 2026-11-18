package com.shoploc.shoploccatalogservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categorie", schema = "catalog")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categorie")
    private Long id;

    @Column(name = "libelle", nullable = false)
    private String label;
}