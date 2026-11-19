package com.shoploc.shoploccatalogservice.entity;

import com.shoploc.shoploccatalogservice.repository.CategoryRepository;
import com.shoploc.shoploccatalogservice.repository.CommerceRepository;
import com.shoploc.shoploccatalogservice.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class CatalogSchemaTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private CategoryRepository categories;

    @Autowired
    private CommerceRepository commerces;

    @Autowired
    private ProductRepository products;

    private List<?> columnsOf(String table) {
        return em.createNativeQuery(
                "select lower(column_name) from information_schema.columns "
                        + "where table_schema = 'CATALOG' and table_name = '" + table + "'")
                .getResultList();
    }

    @Test
    void schemaCatalogShouldContainMldTables() {
        List<?> tables = em.createNativeQuery(
                "select lower(table_name) from information_schema.tables where table_schema = 'CATALOG'")
                .getResultList();

        assertTrue(tables.containsAll(List.of("commerce", "categorie", "produit", "horaire")),
                "Tables trouvées : " + tables);
    }

    @Test
    void tableColumnsShouldMatchMld() {
        assertTrue(columnsOf("PRODUIT").containsAll(List.of(
                "id_produit", "nom", "description", "prix_unitaire", "stock",
                "seuil_alerte", "photo_url", "actif", "id_commerce", "id_categorie")));

        assertTrue(columnsOf("COMMERCE").containsAll(List.of(
                "id_commerce", "nom", "adresse", "telephone", "actif", "id_commercant")));

        assertTrue(columnsOf("CATEGORIE").containsAll(List.of(
                "id_categorie", "libelle")));

        assertTrue(columnsOf("HORAIRE").containsAll(List.of(
                "id_horaire", "jour", "heure_ouverture", "heure_fermeture", "id_commerce")));
    }

    @Test
    void productShouldBePersistedWithItsCommerceCategoryAndOpeningHours() {
        var category = categories.save(Category.builder().label("Boulangerie").build());
        var commerce = commerces.save(Commerce.builder()
                .name("Chez Paul").address("1 rue A").idCommercant("kc-user-1").build());

        em.persist(OpeningHour.builder()
                .day(DayOfWeek.MONDAY)
                .openingTime(LocalTime.of(8, 0))
                .closingTime(LocalTime.of(19, 0))
                .commerce(commerce)
                .build());

        var saved = products.save(Product.builder()
                .name("Baguette").description("Tradition")
                .price(new BigDecimal("1.20")).stock(10)
                .commerce(commerce).category(category)
                .build());

        em.flush();
        em.clear();

        var reloaded = products.findById(saved.getId()).orElseThrow();
        assertEquals("Chez Paul", reloaded.getCommerce().getName());
        assertEquals("Boulangerie", reloaded.getCategory().getLabel());
        assertEquals(0, reloaded.getAlertThreshold());
        assertTrue(reloaded.getActive());
        assertEquals(1, products.findByCommerce_IdCommercant("kc-user-1", PageRequest.of(0, 10))
                .getTotalElements());
    }
}