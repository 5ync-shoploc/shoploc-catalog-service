package com.shoploc.shoploccatalogservice.controller;

import com.shoploc.shoploccatalogservice.dto.InternalProductResponse;
import com.shoploc.shoploccatalogservice.dto.StockDecrementRequest;
import com.shoploc.shoploccatalogservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductInternalControllerTest {

    private ProductService service;
    private ProductController controller;

    @BeforeEach
    void setUp() {
        service = mock(ProductService.class);
        controller = new ProductController(service);
    }

    @Test
    void getProductInternalShouldReturnProductWithStockAndMerchantId() {
        var response = new InternalProductResponse(1L, "Pain", new BigDecimal("3.50"), true, 100, "kc-merchant-1");
        when(service.getInternalProduct(1L)).thenReturn(response);

        InternalProductResponse result = controller.getProductInternal(1L);

        assertEquals(100, result.stock());
        assertEquals("kc-merchant-1", result.merchantId());
        verify(service).getInternalProduct(1L);
    }

    @Test
    void getProductInternalUnknownIdShouldPropagateNotFound() {
        when(service.getInternalProduct(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Article introuvable"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.getProductInternal(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void decrementStockShouldReturnNoContent() {
        var request = new StockDecrementRequest(List.of(
                new StockDecrementRequest.Item(1L, 2),
                new StockDecrementRequest.Item(3L, 1)));

        ResponseEntity<Void> response = controller.decrementStock(request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service).decrementStock(request);
    }

    @Test
    void decrementStockInsufficientStockShouldPropagateConflict() {
        var request = new StockDecrementRequest(List.of(new StockDecrementRequest.Item(1L, 500)));
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuffisant"))
                .when(service).decrementStock(request);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.decrementStock(request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }
}