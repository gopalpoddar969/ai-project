package com.digital.commerceai.rag;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

/**
 * Builds textual context from documents retrieved from the vector store.
 */
@Service
public class RagContextService {

    /**
     * Combines retrieved documents into a single context string for the AI model.
     *
     * @param documents retrieved documents
     * @return combined document context or a message when no relevant information exists
     */
    public String buildContext(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return "No relevant business information was found.";
        }
        return documents.stream()
                .map(Document::getText)
                .reduce("", (a, b) -> a + "\n\n" + b);
    }
}