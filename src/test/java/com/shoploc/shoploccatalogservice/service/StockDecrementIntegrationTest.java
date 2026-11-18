package com.shoploc.shoploccatalogservice.service;

import com.shoploc.shoploccatalogservice.dto.StockDecrementRequest;
import com.shoploc.shoploccatalogservice.entity.Category;
import com.shoploc.shoploccatalogservice.entity.Commerce;
import com.shoploc.shoploccatalogservice.entity.Product;
import com.shoploc.shoploccatalogservice.repository.CategoryRepository;
import com.shoploc.shoploccatalogservice.repository.CommerceRepository;
import com.shoploc.shoploccatalogservice.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@ActiveProfiles("test")
@SpringBootTest
class StockDecrementIntegrationTest {

    @Autowired private ProductService service;
    @Autowired private ProductRepository products;
    @Autowired private CommerceRepository commerces;
    @Autowired private CategoryRepository categories;

    private Product p1;
    private Product p2;

    @BeforeEach
    void setUp() {
        var category = categories.save(Category.builder().label("Boulangerie").build());
        var commerce = commerces.save(Commerce.builder()
                .name("Chez Paul").address("1 rue A").idCommercant("kc-merchant-1").build());
        p1 = products.save(newProduct("Pain", 10, commerce, category));
        p2 = products.save(newProduct("Croissant", 5, commerce, category));
    }

    @AfterEach
    void cleanUp() {
        products.deleteAll();
        commerces.deleteAll();
        categories.deleteAll();
    }

    private static Product newProduct(String name, int stock, Commerce commerce, Category category) {
        return Product.builder().name(name).description("d").price(new BigDecimal("3.50"))
                .stock(stock).commerce(commerce).category(category).build();
    }

    private static StockDecrementRequest request(StockDecrementRequest.Item... items) {
        return new StockDecrementRequest(List.of(items));
    }

    private int stockOf(Product p) {
        return products.findById(p.getId()).orElseThrow().getStock();
    }

    @Test
    void decrementShouldPersistNewStocksAndMergeDuplicates() {
        service.decrementStock(request(
                new StockDecrementRequest.Item(p1.getId(), 2),
                new StockDecrementRequest.Item(p2.getId(), 1),
                new StockDecrementRequest.Item(p1.getId(), 3)));

        assertEquals(5, stockOf(p1));
        assertEquals(4, stockOf(p2));
    }

    @Test
    void insufficientStockShouldReturnConflictAndLeaveAllStocksUntouched() {
        var ex = assertThrows(ResponseStatusException.class, () -> service.decrementStock(request(
                new StockDecrementRequest.Item(p1.getId(), 2),
                new StockDecrementRequest.Item(p2.getId(), 99))));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals(10, stockOf(p1));
        assertEquals(5, stockOf(p2));
    }

    @Test
    void unknownProductShouldReturnNotFoundAndLeaveAllStocksUntouched() {
        var ex = assertThrows(ResponseStatusException.class, () -> service.decrementStock(request(
                new StockDecrementRequest.Item(p1.getId(), 1),
                new StockDecrementRequest.Item(999_999L, 1))));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals(10, stockOf(p1));
    }

    @Test
    void internalReadShouldExposeExactStockAndMerchantId() {
        var result = service.getInternalProduct(p1.getId());

        assertEquals(10, result.stock());
        assertEquals("kc-merchant-1", result.merchantId());
        assertTrue(result.active());
    }
}