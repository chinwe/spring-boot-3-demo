package com.example.demo;

import com.example.demo.dto.UserDto;
import com.example.demo.mapper.UserMapper;
import com.example.demo.vo.DelayVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TestController 单元测试
 * 使用 @WebMvcTest + MockitoBean 进行 Web 层测试
 */
@WebMvcTest(TestController.class)
class TestControllerMockitoTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private DSLContext dsl;

    @MockitoBean
    private UserMapper userMapper;

    // ==================== hello 端点 ====================

    @Test
    @DisplayName("GET /test/hello — 返回问候语")
    void testSayHello() throws Exception {
        mockMvc.perform(get("/test/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello from TestController!"));
    }

    // ==================== echo 端点 ====================

    @Test
    @DisplayName("POST /test/echo — 回显消息")
    void testEcho() throws Exception {
        mockMvc.perform(post("/test/echo")
                        .contentType("text/plain")
                        .content("Hello World"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello World"));
    }

    // ==================== delay 端点 ====================

    @Test
    @DisplayName("POST /test/delay — 延迟响应（1秒）")
    void testDelay() throws Exception {
        DelayVo delayVo = DelayVo.builder().second(1).build();

        mockMvc.perform(post("/test/delay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(delayVo)))
                .andExpect(status().isOk())
                .andExpect(content().string("Delayed for 1 seconds"));
    }

    // ==================== user 端点 ====================

    @Test
    @DisplayName("GET /test/user — 获取用户 DTO")
    void testGetUser() throws Exception {
        // Given
        UserDto userDto = new UserDto();
        userDto.setName("John");
        userDto.setEmail("john@example.com");
        when(userMapper.toUserDto(any())).thenReturn(userDto);

        // When & Then
        mockMvc.perform(get("/test/user"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("John")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("john@example.com")));
    }
}
