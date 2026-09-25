package com.digital.commerceai.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import com.digital.commerceai.memory.ConversationMemoryService;
import com.digital.commerceai.rag.RagContextService;
import com.digital.commerceai.rag.RagRetrievalService;
import com.digital.commerceai.rag.RagService;
import com.digital.commerceai.rag.model.SemanticProductResponse;
import com.digital.commerceai.rag.model.SemanticSearchResponse;
import com.digital.commerceai.tool.InventoryLookupTool;
import com.digital.commerceai.tool.ProductSearchTool;

/**
 * Service responsible for handling general AI conversations, product queries,
 * RAG queries, semantic product searches, agentic tool execution,
 * and conversation memory.
 */
@Service
public class AiService {
    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final ChatClient chatClient;
    private final AiPromptService aiPromptService;
    private final RagService ragService;
    private final RagRetrievalService ragRetrievalService;
    private final RagContextService ragContextService;
    private final ConversationMemoryService conversationMemoryService;
    private final ProductSearchTool productSearchTool;
    private final InventoryLookupTool inventoryLookupTool;

    public AiService(
            ChatClient.Builder chatClientBuilder,
            AiPromptService aiPromptService,
            RagService ragService,
            RagRetrievalService ragRetrievalService,
            RagContextService ragContextService,
            ConversationMemoryService conversationMemoryService,
            ProductSearchTool productSearchTool,
            InventoryLookupTool inventoryLookupTool) {

        this.chatClient = chatClientBuilder.build();
        this.aiPromptService = aiPromptService;
        this.ragService = ragService;
        this.ragRetrievalService = ragRetrievalService;
        this.ragContextService = ragContextService;
        this.conversationMemoryService = conversationMemoryService;
        this.productSearchTool = productSearchTool;
        this.inventoryLookupTool = inventoryLookupTool;
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
     * PRODUCT CHAT / AGENTIC TOOL CALLING
     * ============================================================
     */

    /**
     * Processes a product-related AI question while maintaining conversation
     * history and allowing the AI model to autonomously use product and
     * inventory tools.
     *
     * <p>The AI model can use the product search tool, the inventory lookup
     * tool, or multiple tools when the user's question requires information
     * from multiple business capabilities.</p>
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

                    Use the available business tools when
                    reliable product or inventory information
                    is required.

                    You may use one or more tools when the
                    current question requires information from
                    multiple business capabilities.

                    Use previous conversation only to maintain
                    conversational continuity.

                    Do not invent product information.

                    Do not mention the internal conversation ID
                    or memory mechanism.
                    """.formatted(history, question))
                .tools(productSearchTool, inventoryLookupTool)
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
     * Processes a RAG-based question using retrieved business information
     * and conversation history.
     *
     * @param question current user question
     * @param conversationId conversation identifier
     * @return generated RAG response
     */
    public String ragQuery(String question, String conversationId) {
        conversationId = ensureConversationId(conversationId);

        String history = conversationMemoryService.buildHistory(conversationId);

        List<Document> documents = ragRetrievalService.retrieve(question);

        String context;

        if (documents == null || documents.isEmpty()) {
            context = ragService.getDemoKnowledge();
        } else {
            context = ragContextService.buildContext(documents);
        }

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
     * Performs semantic product search and generates a conversational
     * response using retrieved products and conversation history.
     *
     * @param question current product search question
     * @param conversationId conversation identifier
     * @return semantic product search response
     */
    public SemanticSearchResponse semanticSearch(
            String question,
            String conversationId) {

        conversationId = ensureConversationId(conversationId);

        /*
         * Product semantic search uses the product-specific
         * retrieval method so that only product documents
         * are returned and duplicate products are removed.
         */
        List<Document> documents =
                ragRetrievalService.retrieveProducts(question);

        SemanticSearchResponse response = new SemanticSearchResponse();

        response.setQuery(question);
        response.setSearchType("SEMANTIC_PRODUCT_SEARCH");

        if (documents == null || documents.isEmpty()) {
            response.setSemanticMatchesFound(false);
            response.setAnswer(
                    "I could not find any relevant products for your request."
            );
            return response;
        }

        response.setSemanticMatchesFound(true);

        List<SemanticProductResponse> products = new ArrayList<>();

        for (Document document : documents) {
            String partNumber =
                    extractField(document.getText(), "Part number:");

            String catentryId =
                    extractField(document.getText(), "Catentry ID:");

            String name =
                    extractField(document.getText(), "Product name:");

            String manufacturer =
                    extractField(document.getText(), "Manufacturer:");

            String shortDescription =
                    extractField(document.getText(), "Short description:");

            /*
             * catentryId is optional because your current
             * vector document text may not contain it.
             */
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
            response.setAnswer(
                    "I could not find any relevant products " +
                    "for your request."
            );
            return response;
        }

        /*
         * Fetch previous conversation.
         */
        String history =
                conversationMemoryService.buildHistory(conversationId);

        String context =
                ragContextService.buildContext(documents);

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
        conversationMemoryService.addMessage(
                conversationId,
                "USER",
                question
        );

        conversationMemoryService.addMessage(
                conversationId,
                "ASSISTANT",
                answer
        );

        return response;
    }

    /**
     * Processes a Commerce task using an agentic workflow.
     *
     * <p>The AI model is given the available Commerce business
     * tools and can autonomously determine which tools are required
     * to complete the user's task.</p>
     *
     * <p>The local Commerce tools provide product search and
     * inventory lookup capabilities.</p>
     *
     * <p>A unique trace identifier is generated for each agent
     * execution so that the beginning and completion of an AI
     * workflow can be correlated in application logs.</p>
     *
     * @param question Commerce task to execute
     * @param conversationId conversation identifier
     * @return final response generated by the agentic workflow
     */
    public String agentQuery(String question, String conversationId) {

        String traceId = UUID.randomUUID().toString();

        log.info(
                "AI AGENT START - Trace ID: {} - Question: {}",
                traceId,
                question
        );

        conversationId = ensureConversationId(conversationId);

        String history =
                conversationMemoryService.buildHistory(conversationId);

        String answer = chatClient.prompt()
                .system("""
                    You are an autonomous Commerce AI agent.

                    Your responsibility is to complete the user's
                    Commerce task by reasoning about the task and
                    using the available business tools when required.

                    Follow this workflow:

                    1. Understand the user's objective.
                    2. Determine what information is required.
                    3. Select the appropriate business tool.
                    4. Execute the tool.
                    5. Inspect the tool result.
                    6. Determine whether additional tool calls are required.
                    7. Continue until sufficient information is available.
                    8. Provide a concise final answer.

                    Tool usage rules:

                    - Use product search when product information is required.
                    - Use inventory lookup when inventory information is required.
                    - You may execute multiple tool calls when required.
                    - Product search provides product information only.
                    - Inventory lookup is the authoritative source for inventory.
                    - Use the inventory status returned by the inventory tool.
                    - Do not independently redefine inventory status rules.
                    - Do not assume that a product search result represents
                    inventory availability.
                    - Do not invent product information.
                    - Do not invent inventory quantities.
                    - Do not claim a product is available unless the available
                    business information supports that conclusion.

                    Commerce safety rules:

                    - Do not place orders.
                    - Do not modify customer information.
                    - Do not modify product information.
                    - Do not perform financial transactions.
                    - Do not claim that an action was completed unless an
                    available tool actually performed that action.

                    The final response must contain only the user-facing answer.
                    """)
                .user("""
                        Previous conversation:

                        %s

                        Current task:

                        %s

                        Complete the task using the available tools
                        when required.

                        Return only the final user-facing answer.
                        """.formatted(history, question))
                .tools(
                        productSearchTool,
                        inventoryLookupTool
                )
                .call()
                .content();

        conversationMemoryService.addMessage(
                conversationId,
                "USER",
                question
        );

        conversationMemoryService.addMessage(
                conversationId,
                "ASSISTANT",
                answer
        );

        log.info(
                "AI AGENT END - Trace ID: {}",
                traceId
        );

        return answer;
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

        String value =
                text.substring(start, end).trim();

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