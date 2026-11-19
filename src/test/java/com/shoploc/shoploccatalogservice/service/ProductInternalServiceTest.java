package com.shoploc.shoploccatalogservice.service;

import com.shoploc.shoploccatalogservice.dto.InternalProductResponse;
import com.shoploc.shoploccatalogservice.dto.StockDecrementRequest;
import com.shoploc.shoploccatalogservice.entity.Commerce;
import com.shoploc.shoploccatalogservice.entity.Product;
import com.shoploc.shoploccatalogservice.repository.CategoryRepository;
import com.shoploc.shoploccatalogservice.repository.CommerceRepository;
import com.shoploc.shoploccatalogservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductInternalServiceTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private CommerceRepository commerceRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService service;

    private static Product product(long id, int stock, boolean active) {
        return Product.builder()
                .id(id).name("Produit " + id).description("d")
                .price(new BigDecimal("3.50")).stock(stock).active(active)
                .commerce(Commerce.builder().id(10L).idCommercant("kc-merchant-1").build())
                .build();
    }

    private static StockDecrementRequest request(Object... pairs) {
        var items = new java.util.ArrayList<StockDecrementRequest.Item>();
        for (int i = 0; i < pairs.length; i += 2) {
            items.add(new StockDecrementRequest.Item((Long) pairs[i], (Integer) pairs[i + 1]));
        }
        return new StockDecrementRequest(items);
    }

    @Test
    void getInternalProductShouldReturnStockAndMerchantId() {
        when(repository.findById(1L)).thenReturn(Optional.of(product(1L, 100, true)));

        InternalProductResponse result = service.getInternalProduct(1L);

        assertEquals(1L, result.id());
        assertEquals("Produit 1", result.name());
        assertEquals(new BigDecimal("3.50"), result.price());
        assertTrue(result.active());
        assertEquals(100, result.stock());
        assertEquals("kc-merchant-1", result.merchantId());
    }

    @Test
    void getInternalProductInactiveShouldStillBeReturnedWithActiveFalse() {
        when(repository.findById(2L)).thenReturn(Optional.of(product(2L, 5, false)));

        assertFalse(service.getInternalProduct(2L).active());
    }

    @Test
    void getInternalProductUnknownShouldThrowNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getInternalProduct(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void decrementStockShouldDecrementAllProducts() {
        var p1 = product(1L, 10, true);
        var p3 = product(3L, 5, true);
        when(repository.findAllByIdInForUpdate(anyCollection())).thenReturn(List.of(p1, p3));

        service.decrementStock(request(1L, 2, 3L, 1));

        assertEquals(8, p1.getStock());
        assertEquals(4, p3.getStock());
    }

    @Test
    void decrementStockShouldSumQuantitiesOfDuplicatedProduct() {
        var p1 = product(1L, 10, true);
        when(repository.findAllByIdInForUpdate(anyCollection())).thenReturn(List.of(p1));

        service.decrementStock(request(1L, 2, 1L, 3));

        assertEquals(5, p1.getStock());
    }

    @Test
    void decrementStockDuplicatesExceedingStockShouldThrowConflict() {
        var p1 = product(1L, 4, true);
        when(repository.findAllByIdInForUpdate(anyCollection())).thenReturn(List.of(p1));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.decrementStock(request(1L, 2, 1L, 3)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals(4, p1.getStock());
    }

    @Test
    void decrementStockInsufficientStockShouldThrowConflictAndChangeNothing() {
        var p1 = product(1L, 10, true);
        var p3 = product(3L, 1, true);
        when(repository.findAllByIdInForUpdate(anyCollection())).thenReturn(List.of(p1, p3));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.decrementStock(request(1L, 2, 3L, 5)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals(10, p1.getStock());
        assertEquals(1, p3.getStock());
    }

    @Test
    void decrementStockUnknownProductShouldThrowNotFoundAndChangeNothing() {
        var p1 = product(1L, 10, true);
        when(repository.findAllByIdInForUpdate(anyCollection())).thenReturn(List.of(p1));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.decrementStock(request(1L, 2, 3L, 1)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals(10, p1.getStock());
    }
}