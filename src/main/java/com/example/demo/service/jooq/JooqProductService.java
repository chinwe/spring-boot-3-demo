package com.example.demo.service.jooq;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.jooq.JooqCreateProductRequest;
import com.example.demo.dto.jooq.JooqProductDto;
import com.example.demo.repository.jooq.JooqProductRepository;

import lombok.RequiredArgsConstructor;

/**
 * JOOQ 商品服务
 *
 * @author chinwe
 */
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
        JooqProductDto product = JooqProductDto.builder()
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .stock(request.getStock())
            .category(request.getCategory())
            .build();
        return productRepository.insert(product);
    }

    /**
     * 根据 ID 查询商品
     *
     * @param id 商品 ID
     * @return 商品 DTO
     */
    public JooqProductDto getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * 批量创建商品
     *
     * @param requests 创建商品请求列表
     */
    public void batchCreateProducts(List<JooqCreateProductRequest> requests) {
        List<JooqProductDto> products = requests.stream()
            .map(r -> JooqProductDto.builder()
                .name(r.getName())
                .description(r.getDescription())
                .price(r.getPrice())
                .stock(r.getStock())
                .category(r.getCategory())
                .build())
            .toList();
        productRepository.batchInsert(products);
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
