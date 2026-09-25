package com.digital.commerceai.tool;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.digital.commerceai.rag.RagContextService;
import com.digital.commerceai.rag.RagRetrievalService;

/**
 * Provides product-search capabilities to the AI model through
 * Spring AI tool calling.
 *
 * <p>The tool uses the existing semantic-search and vector-store
 * implementation rather than implementing a separate search mechanism.</p>
 */
@Component
public class ProductSearchTool {

    private final RagRetrievalService ragRetrievalService;
    private final RagContextService ragContextService;

    /**
     * Creates the product search tool.
     *
     * @param ragRetrievalService service used to retrieve semantically
     *                            relevant product documents
     * @param ragContextService service used to convert retrieved
     *                          documents into model-readable context
     */
    public ProductSearchTool(
            RagRetrievalService ragRetrievalService,
            RagContextService ragContextService) {

        this.ragRetrievalService = ragRetrievalService;
        this.ragContextService = ragContextService;
    }

    /**
     * Searches the Commerce product knowledge base using semantic search.
     *
     * <p>The AI model should use this tool when the user asks to find,
     * search for, identify, or discover Commerce products.</p>
     *
     * @param question natural-language product search request
     * @return retrieved product information that can be used by the AI model
     */
    @Tool(
            name = "searchProducts",
            description = """
                    Search the Commerce product catalog using semantic search.

                    Use this tool when the customer wants to find, search,
                    identify, or discover products based on natural-language
                    product requirements, product names, descriptions,
                    characteristics, or product attributes.

                    Do not use this tool for general knowledge questions.
                    Do not invent product information when the tool returns
                    no relevant results.
                    """
    )
    public String searchProducts(
            @ToolParam(
                    description = "Natural-language description of the product the customer wants to find"
            )
            String question) {

        System.out.println();
        System.out.println("==============================================");
        System.out.println("AI TOOL CALL");
        System.out.println("Tool      : searchProducts");
        System.out.println("Question  : " + question);
        System.out.println("==============================================");

        List<Document> documents = ragRetrievalService.retrieve(question);

        System.out.println("Product search returned documents: " + (documents == null ? 0 : documents.size()));

        if (documents == null || documents.isEmpty()) {

            System.out.println("Tool result: No products found.");
            System.out.println("==============================================");

            return "No relevant products were found for the customer's request.";
        }

        String context = ragContextService.buildContext(documents);

        System.out.println("Tool result generated.");
        System.out.println("==============================================");

        return context;
    }
}