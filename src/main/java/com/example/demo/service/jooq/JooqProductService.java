package com.example.demo.service.jooq;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.jooq.JooqCreateProductRequest;
import com.example.demo.dto.jooq.JooqProductDto;
import com.example.demo.exception.JooqExceptionHandler.EntityNotFoundException;
import com.example.demo.repository.jooq.JooqProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JOOQ 商品服务
 *
 * @author chinwe
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JooqProductService {

    private final JooqProductRepository productRepository;

    /**
     * 创建商品
     *
     * @param request 创建商品请求
     * @return 商品 ID
     */
    public Long createProduct(JooqCreateProductRequest request) {
        log.debug("Creating product: {}", request.getName());
        JooqProductDto product = JooqProductDto.builder()
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .stock(request.getStock())
            .category(request.getCategory())
            .build();
        Long id = productRepository.insert(product);
        log.info("Product created successfully with id: {}", id);
        return id;
    }

    /**
     * 根据 ID 查询商品
     *
     * @param id 商品 ID
     * @return 商品 DTO
     * @throws EntityNotFoundException 商品不存在时抛出
     */
    public JooqProductDto getProductById(Long id) {
        log.debug("Fetching product by id: {}", id);
        JooqProductDto product = productRepository.findById(id);
        if (product == null) {
            log.warn("Product not found with id: {}", id);
            throw new EntityNotFoundException("Product not found with id: " + id);
        }
        return product;
    }

    /**
     * 批量创建商品
     *
     * @param requests 创建商品请求列表
     * @throws IllegalStateException 如果批量插入部分失败
     */
    public void batchCreateProducts(List<JooqCreateProductRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            log.warn("Batch create products called with empty list");
            return;
        }

        log.info("Batch creating {} products", requests.size());
        List<JooqProductDto> products = requests.stream()
            .map(r -> JooqProductDto.builder()
                .name(r.getName())
                .description(r.getDescription())
                .price(r.getPrice())
                .stock(r.getStock())
                .category(r.getCategory())
                .build())
            .toList();

        int[] results = productRepository.batchInsert(products);

        // 验证结果
        int successCount = 0;
        int failureCount = 0;
        List<Integer> failedIndexes = new ArrayList<>();

        for (int i = 0; i < results.length; i++) {
            if (results[i] > 0) {
                successCount++;
            } else {
                failureCount++;
                failedIndexes.add(i);
            }
        }

        if (failureCount > 0) {
            log.error("Batch insert partially failed: {} succeeded, {} failed. Failed indexes: {}",
                successCount, failureCount, failedIndexes);
            throw new IllegalStateException(
                String.format("Batch insert partially completed: %d succeeded, %d failed",
                    successCount, failureCount)
            );
        }

        log.info("Batch insert completed successfully, {} products created", successCount);
    }

    /**
     * 根据分类查询商品（分页）
     *
     * @param category 分类名称
     * @param page 页码（从 0 开始）
     * @param size 每页大小
     * @return 商品列表
     */
    public List<JooqProductDto> getProductsByCategory(String category, int page, int size) {
        return productRepository.findByCategory(category, page * size, size);
    }

    /**
     * 更新商品
     *
     * @param product 商品 DTO
     * @return 是否成功
     */
    public boolean updateProduct(JooqProductDto product) {
        return productRepository.upsert(product);
    }

    /**
     * 获取指定分类的总库存
     *
     * @param category 分类名称
     * @return 总库存
     */
    public int getTotalStockByCategory(String category) {
        return productRepository.getTotalStockByCategory(category);
    }

    /**
     * 查询库存低于阈值的商品
     *
     * @param threshold 库存阈值
     * @return 商品列表
     */
    public List<JooqProductDto> getLowStockProducts(int threshold) {
        return productRepository.findLowStockProducts(threshold);
    }
}
