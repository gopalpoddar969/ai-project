package com.digital.commerceai.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.digital.commerceai.rag.model.SemanticSearchResponse;
import com.digital.commerceai.service.AiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**

* REST controller that exposes AI, product, RAG, semantic search,
* and conversation management endpoints.
*
* <p>
* The controller accepts an optional {@code X-Conversation-Id} request
* header for conversational requests. The conversation identifier is
* managed by the backend and is used to maintain conversation history.
* </p>

*/
@RestController
@Tag(
name = "AI Assistant",
description = "AI, RAG, semantic product search and conversation APIs"
)
public class AiController {

private final AiService aiService;

/**
 * Creates the AI controller with the required AI service.
 *
 * @param aiService service responsible for processing AI requests
 */
public AiController(AiService aiService) {
    this.aiService = aiService;
}

/**
 * Processes a general AI question.
 *
 * @param question user question
 * @param conversationId optional conversation identifier supplied
 *                       through the X-Conversation-Id request header
 * @return generated AI response
 */
@Operation(
        summary = "Ask a general AI question",
        description = """
                Sends a general question to the Commerce AI Assistant.

                The optional X-Conversation-Id header identifies the
                conversation whose history should be used. Conversation
                identifiers are generated and managed by the backend.
                """
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "AI response generated successfully",
                content = @Content(
                        mediaType = "text/plain",
                        schema = @Schema(type = "string")
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = @Content
        )
})
@GetMapping("/api/ai/ask")
public String generalQuestion(

        @Parameter(
                description = "Question to ask the AI assistant",
                required = true,
                example = "What is HCL Commerce?"
        )
        @RequestParam String question,

        @Parameter(
                description = """
                        Optional conversation identifier used to
                        continue an existing conversation.
                        """,
                required = false,
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        @RequestHeader(
                value = "X-Conversation-Id",
                required = false
        )
        String conversationId) {

    return aiService.generalQuery(question, conversationId);
}

/**
 * Processes a product-related AI question.
 *
 * @param question user product-related question
 * @param conversationId optional conversation identifier supplied
 *                       through the X-Conversation-Id request header
 * @return generated product response
 */
@Operation(
        summary = "Ask a product-related AI question",
        description = """
                Sends a product-related question to the Commerce AI
                Assistant.

                Conversation history can be maintained by supplying
                the X-Conversation-Id request header.
                """
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Product AI response generated successfully",
                content = @Content(
                        mediaType = "text/plain",
                        schema = @Schema(type = "string")
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = @Content
        )
})
@GetMapping("/api/ai/product")
public String productQuestion(

        @Parameter(
                description = "Product-related question",
                required = true,
                example = "What are the available cutting inserts?"
        )
        @RequestParam String question,

        @Parameter(
                description = """
                        Optional conversation identifier used to
                        continue an existing conversation.
                        """,
                required = false,
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        @RequestHeader(
                value = "X-Conversation-Id",
                required = false
        )
        String conversationId) {

    return aiService.productQuery(question, conversationId);
}

/**
 * Processes a RAG-based AI question.
 *
 * @param question user question to be answered using retrieved knowledge
 * @param conversationId optional conversation identifier supplied
 *                       through the X-Conversation-Id request header
 * @return generated RAG response
 */
@Operation(
        summary = "Ask a RAG-based AI question",
        description = """
                Answers a question using retrieved business knowledge
                supplied to the AI model through the RAG workflow.

                Conversation history can be maintained by supplying
                the X-Conversation-Id request header.
                """
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "RAG response generated successfully",
                content = @Content(
                        mediaType = "text/plain",
                        schema = @Schema(type = "string")
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = @Content
        )
})
@GetMapping("/api/ai/rag")
public String ragQuestion(

        @Parameter(
                description = "Question to answer using retrieved knowledge",
                required = true,
                example = "What is the standard delivery time?"
        )
        @RequestParam String question,

        @Parameter(
                description = """
                        Optional conversation identifier used to
                        continue an existing conversation.
                        """,
                required = false,
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        @RequestHeader(
                value = "X-Conversation-Id",
                required = false
        )
        String conversationId) {

    return aiService.ragQuery(question, conversationId);
}

/**
 * Performs semantic product search using the supplied question.
 *
 * @param question natural-language product search question
 * @param conversationId optional conversation identifier supplied
 *                       through the X-Conversation-Id request header
 * @return semantic product search response
 */
@Operation(
        summary = "Perform semantic product search",
        description = """
                Performs semantic product search against the configured
                vector store.

                The response contains the matching product information,
                part numbers, catentry IDs and the conversational answer
                generated from the retrieved product information.
                """
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Semantic product search completed successfully",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(
                                implementation = SemanticSearchResponse.class
                        )
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid search request",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = @Content
        )
})
@GetMapping("/api/ai/semantic-search")
public SemanticSearchResponse semanticSearch(

        @Parameter(
                description = "Natural-language product search query",
                required = true,
                example = "nanoshaft inserts"
        )
        @RequestParam String question,

        @Parameter(
                description = """
                        Optional conversation identifier used to
                        continue an existing conversation.
                        """,
                required = false,
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        @RequestHeader(
                value = "X-Conversation-Id",
                required = false
        )
        String conversationId) {

    return aiService.semanticSearch(question, conversationId);
}

/**
 * Clears the conversation associated with the supplied conversation ID.
 *
 * @param conversationId optional conversation identifier supplied
 *                       through the X-Conversation-Id request header
 */
@Operation(
        summary = "Clear conversation history",
        description = """
                Clears the conversation history associated with the
                supplied X-Conversation-Id.

                The conversation history is maintained in Redis.
                """
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Conversation cleared successfully"
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid conversation identifier",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = @Content
        )
})
@DeleteMapping("/api/ai/conversation")
public void clearConversation(

        @Parameter(
                description = """
                        Conversation identifier whose history should
                        be removed from Redis.
                        """,
                required = false,
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        @RequestHeader(
                value = "X-Conversation-Id",
                required = false
        )
        String conversationId) {

    aiService.clearConversation(conversationId);
}
}
