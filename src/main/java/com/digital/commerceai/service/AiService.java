package com.digital.commerceai.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import com.digital.commerceai.memory.ConversationMemoryService;
import com.digital.commerceai.rag.RagContextService;
import com.digital.commerceai.rag.RagRetrievalService;
import com.digital.commerceai.rag.RagService;
import com.digital.commerceai.rag.model.SemanticProductResponse;
import com.digital.commerceai.rag.model.SemanticSearchResponse;

/**
 * Service responsible for handling general AI conversations, product queries, RAG queries, semantic product searches, and conversation memory.
 */
@Service
public class AiService {

    private final ChatClient chatClient;
    private final AiPromptService aiPromptService;
    private final RagService ragService;
    private final RagRetrievalService ragRetrievalService;
    private final RagContextService ragContextService;
    private final ConversationMemoryService conversationMemoryService;

    /**
     * Creates the AI service with the required AI, RAG, and conversation memory dependencies.
     *
     * @param chatClientBuilder builder used to create the chat client
     * @param aiPromptService service providing AI system prompts
     * @param ragService service providing RAG prompts
     * @param ragRetrievalService service responsible for vector retrieval
     * @param ragContextService service responsible for building retrieved context
     * @param conversationMemoryService service responsible for conversation memory
     */
    public AiService(ChatClient.Builder chatClientBuilder, AiPromptService aiPromptService, RagService ragService, RagRetrievalService ragRetrievalService, RagContextService ragContextService, ConversationMemoryService conversationMemoryService) {
        this.chatClient = chatClientBuilder.build();
        this.aiPromptService = aiPromptService;
        this.ragService = ragService;
        this.ragRetrievalService = ragRetrievalService;
        this.ragContextService = ragContextService;
        this.conversationMemoryService = conversationMemoryService;
    }

    /*
     * ============================================================
     * GENERAL CHAT
     * ============================================================
     */

    /**
     * Processes a general AI question while maintaining conversation history.
     *
     * @param question current user question
     * @param conversationId conversation identifier
     * @return generated AI response
     */
    public String generalQuery(String question, String conversationId) {
        conversationId = ensureConversationId(conversationId);

        String history = conversationMemoryService.buildHistory(conversationId);

        String answer = chatClient.prompt()
                .system(aiPromptService.systemPrompt())
                .user("""
                    Previous conversation:

                    %s

                    Current user question:

                    %s

                    Answer the current question while
                    maintaining continuity with the
                    previous conversation.

                    Do not mention the internal
                    conversation ID or memory mechanism.
                    """.formatted(history, question))
                .call()
                .content();

        conversationMemoryService.addMessage(conversationId, "USER", question);
        conversationMemoryService.addMessage(conversationId, "ASSISTANT", answer);

        return answer;
    }

    /*
     * ============================================================
     * PRODUCT CHAT
     * ============================================================
     */

    /**
     * Processes a product-related AI question while maintaining conversation history.
     *
     * @param question current product question
     * @param conversationId conversation identifier
     * @return generated product response
     */
    public String productQuery(String question, String conversationId) {
        conversationId = ensureConversationId(conversationId);

        String history = conversationMemoryService.buildHistory(conversationId);

        String answer = chatClient.prompt()
                .system(aiPromptService.productSystemPrompt())
                .user("""
                    Previous conversation:

                    %s

                    Current product question:

                    %s

                    Answer the current question while
                    maintaining continuity with the
                    previous conversation.

                    Do not invent product information.
                    """.formatted(history, question))
                .call()
                .content();

        conversationMemoryService.addMessage(conversationId, "USER", question);
        conversationMemoryService.addMessage(conversationId, "ASSISTANT", answer);

        return answer;
    }

    /*
     * ============================================================
     * RAG CHAT
     * ============================================================
     */

    /**
     * Processes a RAG-based question using retrieved business information and conversation history.
     *
     * @param question current user question
     * @param conversationId conversation identifier
     * @return generated RAG response
     */
    public String ragQuery(String question, String conversationId) {
        conversationId = ensureConversationId(conversationId);

        String history = conversationMemoryService.buildHistory(conversationId);

        List<Document> documents = ragRetrievalService.retrieve(question);

        String context = ragContextService.buildContext(documents);

        String answer = chatClient.prompt()
                .system(ragService.ragSystemPrompt())
                .user("""
                    Previous conversation:

                    %s

                    Retrieved business information:

                    %s

                    Current user question:

                    %s

                    Use the retrieved business
                    information as the source of truth.

                    Use the previous conversation only
                    to understand conversational context.

                    Do not invent information.
                    """.formatted(history, context, question))
                .call()
                .content();

        conversationMemoryService.addMessage(conversationId, "USER", question);
        conversationMemoryService.addMessage(conversationId, "ASSISTANT", answer);

        return answer;
    }

    /*
     * ============================================================
     * SEMANTIC PRODUCT SEARCH
     * ============================================================
     */

    /**
     * Performs semantic product search and generates a conversational response using retrieved products and conversation history.
     *
     * @param question current product search question
     * @param conversationId conversation identifier
     * @return semantic product search response
     */
    public SemanticSearchResponse semanticSearch(String question, String conversationId) {
        conversationId = ensureConversationId(conversationId);
        /*
         * For the first version of conversational product
         * search, vector retrieval is still performed using
         * the current question.
         *
         * Conversation memory is supplied to the LLM when
         * generating the final answer.
         */
        List<Document> documents = ragRetrievalService.retrieve(question);

        SemanticSearchResponse response = new SemanticSearchResponse();

        response.setQuery(question);
        response.setSearchType("SEMANTIC_PRODUCT_SEARCH");

        if (documents == null || documents.isEmpty()) {
            response.setSemanticMatchesFound(false);
            response.setAnswer("I could not find any relevant products for your request.");
            return response;
        }

        response.setSemanticMatchesFound(true);

        List<SemanticProductResponse> products = new ArrayList<>();

        for (Document document : documents) {
            String partNumber = extractField(document.getText(), "Part number:");
            String catentryId = extractField(document.getText(), "CatentryId:");
            String name = extractField(document.getText(), "Product name:");
            String manufacturer = extractField(document.getText(), "Manufacturer:");
            String shortDescription = extractField(document.getText(), "Short description:");

            /*
             * catentryId is optional because your current
             * vector document text may not contain it.
             */
            if (partNumber == null && catentryId == null) {
                continue;
            }

            SemanticProductResponse product = new SemanticProductResponse(partNumber, catentryId, name, manufacturer, shortDescription);

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
            response.setAnswer("I could not find any relevant products " +
                    "for your request.");
            return response;
        }

        /*
         * Fetch previous conversation.
         */
        String history = conversationMemoryService.buildHistory(conversationId);

        String context = ragContextService.buildContext(documents);

        String answer = chatClient.prompt()
                .system(ragService.ragSystemPrompt())
                .user("""
                    Previous conversation:

                    %s

                    Retrieved product information:

                    %s

                    Current user question:

                    %s

                    Provide a concise conversational
                    response explaining which products
                    appear relevant to the customer.

                    Use previous conversation only to
                    maintain conversational continuity.

                    Do not invent product IDs,
                    part numbers, specifications,
                    availability or pricing.

                    Do not mention the internal
                    conversation ID or memory mechanism.
                    """.formatted(history, context, question))
                .call()
                .content();

        response.setAnswer(answer);

        /*
         * Store the user question and the final AI response.
         */
        conversationMemoryService.addMessage(conversationId, "USER", question);
        conversationMemoryService.addMessage(conversationId, "ASSISTANT", answer);
        return response;
    }

    /*
     * ============================================================
     * CONVERSATION ID
     * ============================================================
     */

    /**
     * Ensures that a conversation identifier exists for the current request.
     *
     * @param conversationId existing conversation identifier
     * @return existing conversation identifier or a newly generated UUID
     */
    private String ensureConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return UUID.randomUUID().toString();
        }

        return conversationId;
    }

    /*
     * ============================================================
     * TEXT FIELD EXTRACTION
     * ============================================================
     */

    /**
     * Extracts a field value from the stored vector document text.
     *
     * @param text document text containing the field
     * @param marker field marker used to locate the value
     * @return extracted field value or null when the field is unavailable
     */
    private String extractField(String text, String marker) {
        if (text == null || marker == null) {
            return null;
        }

        int start = text.indexOf(marker);

        if (start < 0) {
            return null;
        }

        start += marker.length();

        int end = text.indexOf("\n", start);

        if (end < 0) {
            end = text.length();
        }

        String value = text.substring(start, end).trim();

        return value.isEmpty()
                ? null
                : value;
    }

    /**
     * Clears all stored conversation memory for the specified conversation.
     *
     * @param conversationId the unique identifier of the conversation to clear
     */
    public void clearConversation(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return;
        }
        conversationMemoryService.clearConversation(conversationId);
    }
}