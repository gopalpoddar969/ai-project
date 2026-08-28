package com.digital.commerceai.rag;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public class KnowledgeRepository {

    private final List<KnowledgeDocument> documents = List.of(

            new KnowledgeDocument(
                    "product-001",
                    "Industrial Drill X100",
                    "product",
                    """
                    Industrial Drill X100 is a heavy-duty industrial drilling machine
                    designed for professional manufacturing environments.

                    Features:
                    - High torque motor
                    - Variable speed control
                    - Industrial-grade housing
                    - Suitable for continuous operation

                    The product is available in the B2B product catalog.
                    """
            ),

            new KnowledgeDocument(
                    "inventory-001",
                    "Inventory Information",
                    "inventory",
                    """
                    Industrial Drill X100:
                    Warehouse A: 120 units
                    Warehouse B: 45 units

                    Industrial Cutter C200:
                    Warehouse A: 80 units
                    Warehouse B: 30 units.
                    """
            ),

            new KnowledgeDocument(
                    "pricing-001",
                    "Pricing Information",
                    "pricing",
                    """
                    Industrial Drill X100:
                    Standard B2B price: 1250 USD

                    Industrial Cutter C200:
                    Standard B2B price: 980 USD

                    Final customer pricing may depend on customer-specific
                    contracts, volume agreements, and negotiated discounts.
                    """
            ),

            new KnowledgeDocument(
                    "orders-001",
                    "Order Information",
                    "orders",
                    """
                    B2B customers can place orders through the commerce application.

                    Order processing normally includes:
                    1. Product selection
                    2. Quantity selection
                    3. Price calculation
                    4. Order submission
                    5. Order confirmation.

                    Customer-specific order information is not available
                    in the demo knowledge base.
                    """
            )
    );

    public List<KnowledgeDocument> findAll() {
        return documents;
    }

    public List<KnowledgeDocument> search(String question) {
        String query = question.toLowerCase();
        return documents.stream()
                .filter(document ->
                        document.content().toLowerCase().contains(query)
                        || document.title().toLowerCase().contains(query)
                        || document.category().toLowerCase().contains(query))
                .toList();
    }
}