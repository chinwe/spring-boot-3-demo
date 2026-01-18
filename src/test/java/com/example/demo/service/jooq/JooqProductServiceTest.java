package com.example.demo.service.jooq;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.jooq.JooqCreateProductRequest;
import com.example.demo.dto.jooq.JooqProductDto;
import com.example.demo.exception.JooqExceptionHandler.EntityNotFoundException;
import com.example.demo.mapper.JooqProductMapper;
import com.example.demo.repository.jooq.JooqProductRepository;

/**
 * JooqProductService 单元测试
 * 验证商品服务的功能正确性
 *
 * @author chinwe
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JOOQ 商品服务单元测试")
class JooqProductServiceTest {

    @Mock
    private JooqProductRepository productRepository;

    @Mock
    private JooqProductMapper productMapper;

    @InjectMocks
    private JooqProductService productService;

    private JooqProductDto testProduct;
    private JooqCreateProductRequest testRequest;

    @BeforeEach
    void setUp() {
        testProduct = JooqProductDto.builder()
            .id(1L)
            .name("Test Product")
            .description("Test Description")
            .price(new BigDecimal("99.99"))
            .stock(100)
            .category("Electronics")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        testRequest = JooqCreateProductRequest.builder()
            .name("Test Product")
            .description("Test Description")
            .price(new BigDecimal("99.99"))
            .stock(100)
            .category("Electronics")
            .build();
    }

    @Test
    @DisplayName("成功创建商品")
    void testCreateProduct_Success() {
        // Given
        when(productMapper.toProductDto(testRequest)).thenReturn(testProduct);
        when(productRepository.insert(testProduct)).thenReturn(100L);

        // When
        Long resultId = productService.createProduct(testRequest);

        // Then
        assertEquals(100L, resultId);
        verify(productMapper, times(1)).toProductDto(testRequest);
        verify(productRepository, times(1)).insert(testProduct);
    }

    @Test
    @DisplayName("验证Mapper被调用")
    void testCreateProduct_MapperCalled() {
        // Given
        when(productMapper.toProductDto(testRequest)).thenReturn(testProduct);
        when(productRepository.insert(testProduct)).thenReturn(1L);

        // When
        productService.createProduct(testRequest);

        // Then
        verify(productMapper, times(1)).toProductDto(testRequest);
        verify(productRepository, times(1)).insert(any(JooqProductDto.class));
    }

    @Test
    @DisplayName("根据ID找到商品")
    void testGetProductById_Found() {
        // Given
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(testProduct);

        // When
        JooqProductDto result = productService.getProductById(productId);

        // Then
        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        assertEquals(testProduct.getName(), result.getName());
        assertEquals(testProduct.getPrice(), result.getPrice());
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("商品不存在抛出异常")
    void testGetProductById_NotFound_ThrowsException() {
        // Given
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(null);

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            productService.getProductById(productId);
        });

        assertTrue(exception.getMessage().contains("Product not found with id"));
        assertTrue(exception.getMessage().contains("999"));
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("批量创建成功")
    void testBatchCreateProducts_Success() {
        // Given
        List<JooqCreateProductRequest> requests = Arrays.asList(
            testRequest,
            JooqCreateProductRequest.builder()
                .name("Product 2")
                .price(new BigDecimal("50.00"))
                .stock(50)
                .category("Books")
                .build()
        );

        JooqProductDto product1 = JooqProductDto.builder().name("Product 1").build();
        JooqProductDto product2 = JooqProductDto.builder().name("Product 2").build();

        when(productMapper.toProductDto(any(JooqCreateProductRequest.class)))
            .thenReturn(product1, product2);
        when(productRepository.batchInsert(anyList())).thenReturn(new int[]{1, 1});

        // When
        productService.batchCreateProducts(requests);

        // Then
        verify(productMapper, times(2)).toProductDto(any(JooqCreateProductRequest.class));
        verify(productRepository, times(1)).batchInsert(anyList());
    }

    @Test
    @DisplayName("空列表直接返回")
    void testBatchCreateProducts_EmptyList() {
        // Given
        List<JooqCreateProductRequest> requests = Collections.emptyList();

        // When
        productService.batchCreateProducts(requests);

        // Then
        verify(productMapper, never()).toProductDto(any());
        verify(productRepository, never()).batchInsert(anyList());
    }

    @Test
    @DisplayName("null列表直接返回")
    void testBatchCreateProducts_NullList() {
        // Given
        List<JooqCreateProductRequest> requests = null;

        // When
        productService.batchCreateProducts(requests);

        // Then
        verify(productMapper, never()).toProductDto(any());
        verify(productRepository, never()).batchInsert(anyList());
    }

    @Test
    @DisplayName("部分失败抛出异常")
    void testBatchCreateProducts_PartialFailure_ThrowsException() {
        // Given
        List<JooqCreateProductRequest> requests = Arrays.asList(
            testRequest,
            testRequest,
            testRequest
        );

        when(productMapper.toProductDto(any(JooqCreateProductRequest.class)))
            .thenReturn(testProduct);
        when(productRepository.batchInsert(anyList())).thenReturn(new int[]{1, 0, 1});

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            productService.batchCreateProducts(requests);
        });

        assertTrue(exception.getMessage().contains("partially completed"));
        assertTrue(exception.getMessage().contains("2 succeeded"));
        assertTrue(exception.getMessage().contains("1 failed"));
    }

    @Test
    @DisplayName("分页查询成功")
    void testGetProductsByCategory_Success() {
        // Given
        String category = "Electronics";
        List<JooqProductDto> expectedProducts = Arrays.asList(testProduct);
        when(productRepository.findByCategory(eq(category), eq(0), eq(10)))
            .thenReturn(expectedProducts);

        // When
        List<JooqProductDto> result = productService.getProductsByCategory(category, 0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProduct.getName(), result.get(0).getName());
        verify(productRepository, times(1)).findByCategory(category, 0, 10);
    }

    @Test
    @DisplayName("无结果返回空列表")
    void testGetProductsByCategory_EmptyResult() {
        // Given
        String category = "NonExistent";
        when(productRepository.findByCategory(eq(category), anyInt(), anyInt()))
            .thenReturn(Collections.emptyList());

        // When
        List<JooqProductDto> result = productService.getProductsByCategory(category, 0, 10);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findByCategory(category, 0, 10);
    }

    @Test
    @DisplayName("成功更新商品（Upsert）")
    void testUpdateProduct_Success() {
        // Given
        JooqProductDto productToUpdate = JooqProductDto.builder()
            .id(1L)
            .name("Updated Product")
            .price(new BigDecimal("199.99"))
            .stock(200)
            .category("Electronics")
            .build();

        when(productRepository.upsert(productToUpdate)).thenReturn(true);

        // When
        boolean result = productService.updateProduct(productToUpdate);

        // Then
        assertTrue(result);
        verify(productRepository, times(1)).upsert(productToUpdate);
    }

    @Test
    @DisplayName("获取分类总库存")
    void testGetTotalStockByCategory() {
        // Given
        String category = "Electronics";
        int expectedStock = 500;
        when(productRepository.getTotalStockByCategory(category)).thenReturn(expectedStock);

        // When
        int result = productService.getTotalStockByCategory(category);

        // Then
        assertEquals(expectedStock, result);
        verify(productRepository, times(1)).getTotalStockByCategory(category);
    }

    @Test
    @DisplayName("查询低库存商品")
    void testGetLowStockProducts() {
        // Given
        int threshold = 10;
        List<JooqProductDto> lowStockProducts = Arrays.asList(
            JooqProductDto.builder().name("Product A").stock(5).build(),
            JooqProductDto.builder().name("Product B").stock(8).build()
        );
        when(productRepository.findLowStockProducts(threshold)).thenReturn(lowStockProducts);

        // When
        List<JooqProductDto> result = productService.getLowStockProducts(threshold);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Product A", result.get(0).getName());
        assertEquals(5, result.get(0).getStock());
        verify(productRepository, times(1)).findLowStockProducts(threshold);
    }

    @Test
    @DisplayName("无低库存商品")
    void testGetLowStockProducts_EmptyList() {
        // Given
        int threshold = 5;
        when(productRepository.findLowStockProducts(threshold)).thenReturn(Collections.emptyList());

        // When
        List<JooqProductDto> result = productService.getLowStockProducts(threshold);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findLowStockProducts(threshold);
    }

    @Test
    @DisplayName("单个商品的批量创建")
    void testBatchCreateProducts_SingleItem() {
        // Given
        List<JooqCreateProductRequest> requests = Collections.singletonList(testRequest);
        when(productMapper.toProductDto(testRequest)).thenReturn(testProduct);
        when(productRepository.batchInsert(anyList())).thenReturn(new int[]{1});

        // When
        productService.batchCreateProducts(requests);

        // Then
        verify(productMapper, times(1)).toProductDto(testRequest);
        verify(productRepository, times(1)).batchInsert(anyList());
    }
}
