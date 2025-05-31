package com.example.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestController.class)
@ExtendWith(MockitoExtension.class)
public class TestControllerMockitoTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TestController testControllerMock;

    @Test
    void testSayHelloWithMock() throws Exception {
        // Arrange
        when(testControllerMock.sayHello()).thenReturn("Mocked Hello");

        // Act & Assert
        mockMvc.perform(get("/test/hello"))
            .andExpect(status().isOk())
            .andExpect(content().string("Mocked Hello"));
    }

    @Test
    void testEchoWithMock() throws Exception {
        // Arrange
        when(testControllerMock.echo("Mocked Message")).thenReturn("Mocked Message");

        // Act & Assert
        mockMvc.perform(post("/test/echo")
            .contentType("text/plain")
            .content("Mocked Message"))
            .andExpect(status().isOk())
            .andExpect(content().string("Mocked Message"));
    }

}