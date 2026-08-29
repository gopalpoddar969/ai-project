package com.digital.commerceai.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import com.digital.commerceai.rag.RagContextService;
import com.digital.commerceai.rag.RagRetrievalService;
import com.digital.commerceai.rag.RagService;
import com.digital.commerceai.rag.model.SemanticProductResponse;
import com.digital.commerceai.rag.model.SemanticSearchResponse;

@Service
public class AiService {

    private final ChatClient chatClient;
    private final AiPromptService aiPromptService;
    private final RagService ragService;
    private final RagRetrievalService ragRetrievalService;
    private final RagContextService ragContextService;

    public AiService(
            ChatClient.Builder chatClientBuilder,
            AiPromptService aiPromptService,
            RagService ragService,
            RagRetrievalService ragRetrievalService,
            RagContextService ragContextService) {

        this.chatClient = chatClientBuilder.build();
        this.aiPromptService = aiPromptService;
        this.ragService = ragService;
        this.ragRetrievalService = ragRetrievalService;
        this.ragContextService = ragContextService;
    }


    public String generalQuery(String question) {
        return chatClient.prompt()
                .system(aiPromptService.systemPrompt())
                .user(question)
                .call()
                .content();
    }


    public String productQuery(String question) {
        return chatClient.prompt()
                .system(aiPromptService.productSystemPrompt())
                .user(question)
                .call()
                .content();
    }

    public String ragQuery(String question) {
        List<Document> documents = ragRetrievalService.addDemoKnowledge();
        String context = ragContextService.buildContext(documents);
        return chatClient.prompt()
                .system(ragService.ragSystemPrompt())
                .user("""
                        Retrieved business information:

                        %s

                        User question:

                        %s
                        """.formatted(
                                context,
                                question))
                .call()
                .content();
    }

    public SemanticSearchResponse semanticSearch(String question) {
        List<Document> documents = ragRetrievalService.retrieve(question);
        SemanticSearchResponse response = new SemanticSearchResponse();
        response.setQuery(question);
        response.setSearchType("SEMANTIC_PRODUCT_SEARCH");
        if (documents == null || documents.isEmpty()) {
            response.setSemanticMatchesFound(false);
            response.setAnswer("I could not find any relevant products for your request."
            );
            return response;
        }
        response.setSemanticMatchesFound(true);
        List<SemanticProductResponse> products = new ArrayList<>();
        for (Document document : documents) {
            String text = document.getText();
            String partNumber = extractValue(text, "Part number:");
            String catentryId = extractValue(text, "CatentryId:");
            String name = extractValue(text, "Product name:");
            String manufacturer = extractValue(text, "Manufacturer:");
            String shortDescription = extractValue(text,"Short description:");
            if (partNumber == null && catentryId == null) {
                continue;
            }
            SemanticProductResponse product =
                    new SemanticProductResponse(
                            partNumber,
                            catentryId,
                            name,
                            manufacturer,
                            shortDescription
                    );
            products.add(product);
            if (partNumber != null && !partNumber.isBlank()) {
                response.getPartNumbers().add(partNumber);
            }
            if (catentryId != null && !catentryId.isBlank()) {
                response.getCatentryIds().add(catentryId);
            }
        }
        response.setProducts(products);
        if (products.isEmpty()) {
            response.setSemanticMatchesFound(false);
            response.setAnswer("I could not find any relevant products for your request.");
            return response;
        }
        String context = ragContextService.buildContext(documents);
        String answer =
                chatClient.prompt()
                        .system(
                                ragService.ragSystemPrompt()
                        )
                        .user("""
                                Retrieved product information:

                                %s

                                User question:

                                %s

                                Provide a concise conversational
                                response explaining which products
                                appear relevant to the customer.

                                Do not invent product IDs,
                                part numbers, specifications,
                                availability or pricing.
                                """.formatted(
                                        context,
                                        question))
                        .call()
                        .content();
        response.setAnswer(answer);
        return response;
    }

    private String extractValue(String text, String fieldName) {
        if (text == null || text.isBlank()) {
            return null;
        }
        int start = text.indexOf(fieldName);
        if (start < 0) {
            return null;
        }
        start += fieldName.length();
        int end = text.indexOf("\n", start);
        if (end < 0) {
            end = text.length();
        }
        String value = text.substring(start, end).trim();
        return value.isEmpty() ? null : value;
    }
}