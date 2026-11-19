package com.shoploc.shoploccatalogservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "horaire", schema = "catalog")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpeningHour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horaire")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "jour", nullable = false)
    private DayOfWeek day;

    @Column(name = "heure_ouverture", nullable = false)
    private LocalTime openingTime;

    @Column(name = "heure_fermeture", nullable = false)
    private LocalTime closingTime;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_commerce", nullable = false)
    private Commerce commerce;
}