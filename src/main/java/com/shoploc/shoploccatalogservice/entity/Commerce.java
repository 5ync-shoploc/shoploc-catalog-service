package com.shoploc.shoploccatalogservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "commerce", schema = "catalog")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commerce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_commerce")
    private Long id;

    @Column(name = "nom", nullable = false)
    private String name;

    @Column(name = "adresse", nullable = false)
    private String address;

    @Column(name = "telephone")
    private String phone;

    @Builder.Default
    @Column(name = "actif", nullable = false)
    private Boolean active = true;
    
    @Column(name = "id_commercant", nullable = false, length = 64)
    private String idCommercant;
}