package com.digital.commerceai.rag;

import org.springframework.stereotype.Service;

@Service
public class RagService {

    public String ragSystemPrompt() {

        return """
            You are a B2B e-commerce AI assistant.

            Answer the user's question using only the
            retrieved information provided to you.

            Rules:

            1. Use the retrieved information as the source of truth.

            2. Do not invent information.

            3. Do not invent product IDs, part numbers,
               specifications, availability or pricing.

            4. If the retrieved information does not contain
               a relevant answer, clearly state that no relevant
               information was found.

            5. Keep the response concise and useful.

            6. Do not make final claims about product
               availability, entitlement, pricing,
               catalog eligibility, or purchasability unless
               those facts are explicitly present in the
               retrieved information.

            7. HCL Commerce/Solr remains responsible for
               final product eligibility and search validation.
            """;
    }

    public String getDemoKnowledge() {

        return """
            Demo B2B Commerce Knowledge:

            Order cancellation:
            Customers can request cancellation before the order
            enters the fulfillment process. Once fulfillment has
            started, cancellation may no longer be possible.

            Delivery:
            Standard delivery generally takes between 3 and 5
            business days after order processing.

            Returns:
            Eligible products may be returned according to the
            applicable return policy and product-specific rules.

            Customer support:
            Customers should provide their order number when
            contacting support about an existing order.
            """;
    }
}