package com.digital.commerceai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.digital.commerceai.rag.RagContextService;
import com.digital.commerceai.rag.RagRetrievalService;
import com.digital.commerceai.rag.RagService;

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
        var documents = ragRetrievalService.retrieve(question);
        var context = ragContextService.buildContext(documents);
        return chatClient.prompt()
                .system(ragService.ragSystemPrompt())
                .user("""
                    Retrieved business information:

                    %s

                    User question:

                    %s
                    """.formatted(context, question))
                .call()
                .content();
    }
}