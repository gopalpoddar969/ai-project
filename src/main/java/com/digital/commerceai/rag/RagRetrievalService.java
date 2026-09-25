package com.digital.commerceai.rag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

/**
 * Retrieves semantically similar documents from the vector store for general
 * RAG and product-specific semantic search.
 */
@Service
public class RagRetrievalService {

    private static final int GENERAL_SEARCH_TOP_K = 10;

    /**
     * Retrieves a larger candidate set for product search because multiple
     * highly similar vector documents can belong to the same product.
     */
    private static final int PRODUCT_SEARCH_CANDIDATE_TOP_K = 50;

    /**
     * Maximum number of unique products returned for product search.
     */
    private static final int MAX_UNIQUE_PRODUCTS = 10;

    private static final double SIMILARITY_THRESHOLD = 0.5;

    private final VectorStore vectorStore;

    /**
     * Creates the RAG retrieval service.
     *
     * @param vectorStore vector store used for semantic similarity search
     */
    public RagRetrievalService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Performs semantic similarity search for general RAG knowledge.
     *
     * @param question the user's business knowledge question
     * @return semantically relevant documents
     */
    public List<Document> retrieve(String question) {
        List<Document> rawResults = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(GENERAL_SEARCH_TOP_K)
                        .similarityThreshold(SIMILARITY_THRESHOLD)
                        .build()
        );

        System.out.println();
        System.out.println("==============================================");
        System.out.println("VECTOR SEARCH DEBUG");
        System.out.println("Question : " + question);
        System.out.println("Results  : " + (rawResults == null ? 0 : rawResults.size()));

        if (rawResults != null) {
            int index = 1;

            for (Document document : rawResults) {
                System.out.println("----------------------------------------------");
                System.out.println("Result #" + index++);
                System.out.println("Document ID : " + document.getId());
                System.out.println("Score       : " + document.getScore());
                System.out.println(document.getText());
            }
        }

        System.out.println("==============================================");

        if (rawResults == null || rawResults.isEmpty()) {
            return List.of();
        }

        return rawResults;
    }

    /**
     * Performs semantic similarity search for product documents and removes
     * duplicate products using the Commerce catentry ID or part number
     * extracted directly from the document text.
     *
     * <p>A larger candidate set is retrieved first because topK represents
     * vector documents rather than unique Commerce products. The candidates
     * are then deduplicated before the final product limit is applied.</p>
     *
     * @param question the user's product search question
     * @return unique semantically relevant product documents
     */
    public List<Document> retrieveProducts(String question) {

        List<Document> rawResults = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(PRODUCT_SEARCH_CANDIDATE_TOP_K)
                        .similarityThreshold(SIMILARITY_THRESHOLD)
                        .build()
        );

        System.out.println();
        System.out.println("==============================================");
        System.out.println("PRODUCT VECTOR SEARCH DEBUG");
        System.out.println("Question              : " + question);
        System.out.println("Candidate results     : "
                + (rawResults == null ? 0 : rawResults.size()));

        if (rawResults == null || rawResults.isEmpty()) {
            System.out.println("Unique products        : 0");
            System.out.println("==============================================");
            return List.of();
        }

        Map<String, Document> uniqueProducts = new LinkedHashMap<>();

        for (Document document : rawResults) {

            String catentryId = extractValue(
                    document.getText(),
                    "Catentry ID:");

            String partNumber = extractValue(
                    document.getText(),
                    "Part number:");

            String uniqueKey = buildUniqueProductKey(
                    catentryId,
                    partNumber,
                    document);

            if (uniqueProducts.containsKey(uniqueKey)) {
                continue;
            }

            uniqueProducts.put(uniqueKey, document);

            if (uniqueProducts.size() >= MAX_UNIQUE_PRODUCTS) {
                break;
            }
        }

        List<Document> results = new ArrayList<>(uniqueProducts.values());

        System.out.println("Unique products        : " + results.size());

        int index = 1;

        for (Document document : results) {

            String catentryId = extractValue(
                    document.getText(),
                    "Catentry ID:");

            String partNumber = extractValue(
                    document.getText(),
                    "Part number:");

            System.out.println("----------------------------------------------");
            System.out.println("Product #" + index++);
            System.out.println("Document ID : " + document.getId());
            System.out.println("Catentry ID : " + catentryId);
            System.out.println("Part number : " + partNumber);
            System.out.println("Score       : " + document.getScore());
        }

        System.out.println("==============================================");

        return results;
    }

    /**
     * Adds demo business knowledge documents to the vector store.
     *
     * @return the demo documents added to the vector store
     */
    public List<Document> addDemoKnowledge() {
        List<Document> documents = List.of(
                new Document("""
                        Order cancellation

                        Customers can request cancellation before the order
                        enters the fulfillment process. Once fulfillment has
                        started, cancellation may no longer be possible.
                        """),

                new Document("""
                        Delivery

                        Standard delivery generally takes between 3 and 5
                        business days after order processing.
                        """),

                new Document("""
                        Returns

                        Eligible products may be returned according to the
                        applicable return policy and product-specific rules.
                        """),

                new Document("""
                        Customer support

                        Customers should provide their order number when
                        contacting support about an existing order.
                        """)
        );

        vectorStore.add(documents);

        return documents;
    }

    /**
     * Builds a unique key for a product using the Commerce catentry ID first
     * and the part number as a fallback.
     *
     * @param catentryId Commerce catentry ID extracted from document text
     * @param partNumber Commerce part number extracted from document text
     * @param document vector store document used as final fallback
     * @return unique key used for product deduplication
     */
    private String buildUniqueProductKey(
            String catentryId,
            String partNumber,
            Document document) {

        if (catentryId != null) {
            return "CATENTRY:" + catentryId;
        }

        if (partNumber != null) {
            return "PARTNUMBER:" + partNumber;
        }

        return "DOCUMENT:" + document.getId();
    }

    /**
     * Extracts the value of a named field from document text.
     *
     * @param text document text containing the field
     * @param fieldName field name to locate
     * @return extracted field value, or null when the field is not present
     */
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