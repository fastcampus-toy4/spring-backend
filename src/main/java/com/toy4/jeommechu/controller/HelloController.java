package com.toy4.jeommechu.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/")
    public String home() {
        return "👋 Welcome to Jeommechu Spring Boot App! 김태현 바보";
    }
}
