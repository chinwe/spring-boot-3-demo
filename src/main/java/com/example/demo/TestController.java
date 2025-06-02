package com.example.demo;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.vo.DelayVo;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/test")
public class TestController {
    
    @Resource
    private DSLContext dsl;
    
    @Resource
    private UserMapper userMapper;

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello from TestController!";
    }

    @PostMapping("/echo")
    public String echo(@RequestBody String message) {
        return message;
    }

    @PostMapping("/delay")
    public String delay(@Valid @RequestBody DelayVo delayVo) throws InterruptedException {
        Thread.sleep(delayVo.getSecond() * 1000);
        return String.format("Delayed for %d seconds", delayVo.getSecond());
    }

    @PostMapping("/jooq")
    public String jooq() {
        return dsl.select(DSL.field("CURRENT_TIMESTAMP", String.class))
            .from(DSL.table("DUAL"))
            .fetchOne(0, String.class);
    }

    @GetMapping("/user")
    public String getUser() {
        return userMapper.toUserDto(new User(1L, "John", "john@example.com")).toString();
    }
}
