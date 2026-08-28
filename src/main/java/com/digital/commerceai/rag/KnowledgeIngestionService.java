package com.digital.commerceai.rag;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeIngestionService implements CommandLineRunner {

    private final VectorStore vectorStore;

    public KnowledgeIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) throws Exception {
        loadKnowledge();
    }

    public void loadKnowledge() {
        List<Document> documents = List.of(
            new Document(
                """
                Industrial Drill X100 is a heavy-duty industrial
                drilling machine designed for professional
                manufacturing environments.

                Features:
                - High torque motor
                - Variable speed control
                - Industrial-grade housing
                - Suitable for continuous operation.
                """
            ),
            new Document(
                """
                Industrial Drill X100 inventory:
                Warehouse A: 120 units
                Warehouse B: 45 units.
                """
            ),
            new Document(
                """
                Industrial Drill X100 standard B2B price:
                1250 USD.

                Final customer pricing may depend on customer-specific
                contracts, volume agreements, and negotiated discounts.
                """
            ),
            new Document(
                """
                Industrial Cutter C200 inventory:
                Warehouse A: 80 units
                Warehouse B: 30 units.
                """
            ),
            new Document(
                """
                Industrial Cutter C200 standard B2B price:
                980 USD.
                """
            )
        );
        vectorStore.add(documents);
    }
}