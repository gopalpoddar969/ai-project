package com.digital.commerceai.rag;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

@Service
public class RagContextService {

    public String buildContext(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return "No relevant business information was found.";
        }
        return documents.stream()
                .map(Document::getText)
                .reduce("", (a, b) -> a + "\n\n" + b);
    }
}