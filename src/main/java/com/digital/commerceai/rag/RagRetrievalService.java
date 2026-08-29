package com.digital.commerceai.rag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class RagRetrievalService {

    private final VectorStore vectorStore;

    public RagRetrievalService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<Document> retrieve(String question) {
        List<Document> rawResults = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(10)
                        .similarityThreshold(0.0)
                        .build()
        );
        if (rawResults == null || rawResults.isEmpty()) {
            return List.of();
        }
        Map<String, Document> uniqueProducts = new LinkedHashMap<>();
        for (Document document : rawResults) {
            String catentryId = extractValue(document.getText(), "CatentryId:");
            String partNumber = extractValue(document.getText(), "Part number:");
            String uniqueKey;
            if (catentryId != null) {
                uniqueKey = "CATENTRY:" + catentryId;
            } else if (partNumber != null) {
                uniqueKey = "PARTNUMBER:" + partNumber;
            } else {
                continue;
            }
            uniqueProducts.putIfAbsent(uniqueKey, document);
        }
        List<Document> results = new ArrayList<>(uniqueProducts.values());
        return results;
    }

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