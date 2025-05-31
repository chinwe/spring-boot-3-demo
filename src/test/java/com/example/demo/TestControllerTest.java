package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testSayHello() {
        ResponseEntity<String> response = restTemplate.getForEntity("/test/hello", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Hello from TestController!");
    }

    @Test
    public void testEcho() {
        String inputMessage = "Test message";
        HttpEntity<String> request = new HttpEntity<>(inputMessage);
        ResponseEntity<String> response = restTemplate.exchange("/test/echo", HttpMethod.POST, request, String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(inputMessage);
    }
}