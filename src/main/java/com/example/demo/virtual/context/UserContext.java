package com.example.demo.virtual.context;

import java.lang.ScopedValue;

/**
 * 用户上下文类
 * 使用 ScopedValue 存储线程上下文信息
 *
 * ScopedValue 是 Java 21+ 引入的机制，用于在结构化并发中传递上下文
 * 相比 ThreadLocal，ScopedValue 具有以下优势：
 * 1. 不可变性：值一旦设置就不能修改
 * 2. 自动清理：作用域结束后自动清除
 * 3. 结构化传递：自动传递给子线程
 *
 * 注意：在 JDK 25 中，ScopedValue 已经是正式 API，不再是预览功能
 */
public class UserContext {

    /**
     * 用户 ID
     * 注意：ScopedValue 实例通过工厂方法创建
     */
    public static final ScopedValue<String> USER_ID = ScopedValue.newInstance();

    /**
     * 租户 ID
     */
    public static final ScopedValue<String> TENANT_ID = ScopedValue.newInstance();

    /**
     * 请求 ID
     */
    public static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();

    private UserContext() {
        // 工具类，禁止实例化
    }
}
