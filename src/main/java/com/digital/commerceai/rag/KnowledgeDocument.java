package com.digital.commerceai.rag;

public record KnowledgeDocument(
        String id,
        String title,
        String category,
        String content
) {
}