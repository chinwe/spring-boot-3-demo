package com.example.demo.repository.jooq;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.jooq.JooqProductDto;

/**
 * JooqProductRepository 测试类
 *
 * @author chinwe
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JooqProductRepositoryTest {

    @Autowired
    private DSLContext dsl;

    private JooqProductRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JooqProductRepository(dsl);
    }

    @Test
    void testBatchInsert() {
        // Given
        List<JooqProductDto> products = List.of(
            JooqProductDto.builder()
                .name("Product 1")
                .description("Description 1")
                .price(new BigDecimal("100.00"))
                .stock(50)
                .category("Electronics")
                .build(),
            JooqProductDto.builder()
                .name("Product 2")
                .description("Description 2")
                .price(new BigDecimal("200.00"))
                .stock(30)
                .category("Electronics")
                .build(),
            JooqProductDto.builder()
                .name("Product 3")
                .description("Description 3")
                .price(new BigDecimal("300.00"))
                .stock(20)
                .category("Books")
                .build()
        );

        // When
        int[] result = repository.batchInsert(products);

        // Then
        assertEquals(3, result.length);
        for (int count : result) {
            assertEquals(1, count);
        }

        // Verify products were inserted
        JooqProductDto product1 = repository.findById(1L);
        assertNotNull(product1);
        assertEquals("Product 1", product1.getName());
    }

    @Test
    void testUpsertInsert() {
        // Given - new product
        JooqProductDto product = JooqProductDto.builder()
            .id(999L)
            .name("New Product")
            .description("New Description")
            .price(new BigDecimal("150.00"))
            .stock(25)
            .category("Electronics")
            .build();

        // When
        boolean result = repository.upsert(product);

        // Then
        assertTrue(result);
    }

    @Test
    void testUpsertUpdate() {
        // Given - insert first
        JooqProductDto product = JooqProductDto.builder()
            .name("Product")
            .description("Description")
            .price(new BigDecimal("100.00"))
            .stock(50)
            .category("Electronics")
            .build();
        Long id = repository.insert(product);

        // When - update
        JooqProductDto updatedProduct = JooqProductDto.builder()
            .id(id)
            .name("Updated Product")
            .description("Updated Description")
            .price(new BigDecimal("199.99"))
            .stock(100)
            .category("Books")
            .build();
        boolean result = repository.upsert(updatedProduct);

        // Then
        assertTrue(result);
        JooqProductDto found = repository.findById(id);
        assertEquals("Updated Product", found.getName());
        assertEquals("Updated Description", found.getDescription());
        assertEquals(0, new BigDecimal("199.99").compareTo(found.getPrice()));
        assertEquals(100, found.getStock());
        assertEquals("Books", found.getCategory());
    }

    @Test
    void testFindByCategoryWithPagination() {
        // Given
        repository.batchInsert(List.of(
            JooqProductDto.builder().name("P1").category("Electronics").price(new BigDecimal("100")).stock(10).build(),
            JooqProductDto.builder().name("P2").category("Electronics").price(new BigDecimal("200")).stock(20).build(),
            JooqProductDto.builder().name("P3").category("Electronics").price(new BigDecimal("300")).stock(30).build(),
            JooqProductDto.builder().name("P4").category("Books").price(new BigDecimal("50")).stock(15).build(),
            JooqProductDto.builder().name("P5").category("Books").price(new BigDecimal("60")).stock(25).build()
        ));

        // When - page 1
        List<JooqProductDto> page1 = repository.findByCategory("Electronics", 0, 2);

        // Then
        assertEquals(2, page1.size());
        assertEquals("Electronics", page1.get(0).getCategory());

        // When - page 2
        List<JooqProductDto> page2 = repository.findByCategory("Electronics", 2, 2);

        // Then
        assertEquals(1, page2.size());
    }

    @Test
    void testDecreaseStock() {
        // Given
        JooqProductDto product = JooqProductDto.builder()
            .name("Product")
            .description("Description")
            .price(new BigDecimal("100.00"))
            .stock(100)
            .category("Electronics")
            .build();
        Long id = repository.insert(product);

        // When
        boolean result = repository.decreaseStock(id, 30);

        // Then
        assertTrue(result);
        JooqProductDto updated = repository.findById(id);
        assertEquals(70, updated.getStock());
    }

    @Test
    void testDecreaseStockInsufficient() {
        // Given
        JooqProductDto product = JooqProductDto.builder()
            .name("Product")
            .description("Description")
            .price(new BigDecimal("100.00"))
            .stock(10)
            .category("Electronics")
            .build();
        Long id = repository.insert(product);

        // When
        boolean result = repository.decreaseStock(id, 20);

        // Then
        assertFalse(result);
        JooqProductDto unchanged = repository.findById(id);
        assertEquals(10, unchanged.getStock());
    }

    @Test
    void testDecreaseStockNonExistent() {
        // When
        boolean result = repository.decreaseStock(99999L, 10);

        // Then
        assertFalse(result);
    }

    @Test
    void testGetTotalStockByCategory() {
        // Given
        repository.batchInsert(List.of(
            JooqProductDto.builder().name("P1").category("Electronics").price(new BigDecimal("100")).stock(10).build(),
            JooqProductDto.builder().name("P2").category("Electronics").price(new BigDecimal("200")).stock(20).build(),
            JooqProductDto.builder().name("P3").category("Books").price(new BigDecimal("50")).stock(15).build()
        ));

        // When
        int electronicsStock = repository.getTotalStockByCategory("Electronics");
        int booksStock = repository.getTotalStockByCategory("Books");
        int unknownStock = repository.getTotalStockByCategory("Unknown");

        // Then
        assertEquals(30, electronicsStock);
        assertEquals(15, booksStock);
        assertEquals(0, unknownStock);
    }

    @Test
    void testFindLowStockProducts() {
        // Given
        repository.batchInsert(List.of(
            JooqProductDto.builder().name("P1").category("Electronics").price(new BigDecimal("100")).stock(5).build(),
            JooqProductDto.builder().name("P2").category("Electronics").price(new BigDecimal("200")).stock(15).build(),
            JooqProductDto.builder().name("P3").category("Books").price(new BigDecimal("50")).stock(8).build(),
            JooqProductDto.builder().name("P4").category("Books").price(new BigDecimal("60")).stock(25).build()
        ));

        // When
        List<JooqProductDto> lowStock = repository.findLowStockProducts(10);

        // Then
        assertEquals(2, lowStock.size());
        assertTrue(lowStock.stream().allMatch(p -> p.getStock() < 10));
    }

    @Test
    void testFindById() {
        // Given
        JooqProductDto product = JooqProductDto.builder()
            .name("Test Product")
            .description("Test Description")
            .price(new BigDecimal("99.99"))
            .stock(50)
            .category("Test Category")
            .build();
        Long id = repository.insert(product);

        // When
        JooqProductDto found = repository.findById(id);

        // Then
        assertNotNull(found);
        assertEquals("Test Product", found.getName());
        assertEquals("Test Description", found.getDescription());
        assertEquals(0, new BigDecimal("99.99").compareTo(found.getPrice()));
        assertEquals(50, found.getStock());
        assertEquals("Test Category", found.getCategory());
    }

    @Test
    void testFindByIdNotFound() {
        // When
        JooqProductDto found = repository.findById(99999L);

        // Then
        assertNull(found);
    }

    @Test
    void testInsertNullProduct() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            repository.insert(null);
        });
    }

    @Test
    void testBatchInsertEmpty() {
        // When
        int[] result = repository.batchInsert(List.of());

        // Then
        assertEquals(0, result.length);
    }

    @Test
    void testUpsertNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            repository.upsert(null);
        });
    }
}
