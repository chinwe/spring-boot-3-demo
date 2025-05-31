package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello from TestController!";
    }

    @PostMapping("/echo")
    public String echo(@RequestBody String message) {
        return message;
    }
}
