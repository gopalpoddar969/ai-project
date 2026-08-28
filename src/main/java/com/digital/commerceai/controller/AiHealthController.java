package com.digital.commerceai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiHealthController {

    @GetMapping("/api/ai/health")
    public String health() {
        return "Commerce AI Assistant is running";
    }
}
