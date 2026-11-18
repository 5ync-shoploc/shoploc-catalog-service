@Entity
@Table(name = "produit", schema = "catalog")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produit")
    private Long id;

    @Column(name = "nom", nullable = false)
    private String name;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "prix_unitaire", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Builder.Default
    @Column(name = "seuil_alerte", nullable = false)
    private Integer alertThreshold = 0;

    @Column(name = "photo_url")
    private String imageUrl;

    @Builder.Default
    @Column(name = "actif", nullable = false)
    private Boolean active = true;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_commerce", nullable = false)
    private Commerce commerce;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categorie", nullable = false)
    private Category category;
}