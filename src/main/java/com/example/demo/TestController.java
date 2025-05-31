package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.DelayVo;

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

    @PostMapping("/delay")
    public String delay(@RequestBody DelayVo delayVo) throws InterruptedException {
        Thread.sleep(delayVo.getSecond() * 1000);
        return String.format("Delayed for %d seconds", delayVo.getSecond());
    }
}
