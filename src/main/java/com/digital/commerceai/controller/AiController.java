package com.digital.commerceai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.digital.commerceai.service.AiService;

@RestController
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }
    
    @GetMapping("/api/ai/ask")
    public String generalQuestion(@RequestParam String question) {
        return aiService.generalQuery(question);
    }

    @GetMapping("/api/ai/product")
    public String productQuestion(@RequestParam String question) {
        return aiService.productQuery(question);
    }
    
    @GetMapping("/api/ai/rag")
    public String ragQuestion(@RequestParam String question) {
        return aiService.ragQuery(question);
    }
}
