package com.digital.commerceai.tool;

import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Provides inventory lookup capabilities to the AI model through
 * Spring AI tool calling.
 *
 * <p>The tool currently uses demo inventory data and is intended
 * to simulate an enterprise inventory service.</p>
 */
@Component
public class InventoryLookupTool {

    private final Map<String, Integer> inventory = Map.of(
            "03236192", 25,
            "03236193", 3,
            "03236194", 0,
            "03236195", 12,
            "03236196", 1,
            "03236197", 8,
            "03236198", 4,
            "03236199", 15,
            "03236200", 6,
            "03236201", 2
    );

    /**
     * Checks inventory availability for the supplied product part number.
     *
     * @param partNumber product part number whose inventory should be checked
     * @return inventory information for the requested product
     */
    @Tool(
            name = "checkInventory",
            description = """
                    Check current inventory information for a specific
                    Commerce product using its part number.

                    Use this tool when the customer asks whether a
                    specific product is in stock, available, or has
                    inventory.

                    The tool returns the current inventory quantity
                    and inventory status.

                    Possible inventory statuses are:
                    IN_STOCK
                    LOW_STOCK
                    OUT_OF_STOCK
                    NOT_FOUND
                    """
    )
    public String checkInventory(
            @ToolParam(
                    description = "Exact product part number whose inventory should be checked",
                    required = true
            )
            String partNumber) {

        System.out.println();
        System.out.println("==============================================");
        System.out.println("AI TOOL CALL");
        System.out.println("Tool      : checkInventory");
        System.out.println("Part No   : " + partNumber);
        System.out.println("==============================================");

        if (partNumber == null || partNumber.isBlank()) {
            System.out.println("Tool result: Part number was not provided.");
            System.out.println("==============================================");

            return "Inventory status: NOT_FOUND. No valid part number was provided.";
        }

        String normalizedPartNumber = partNumber.trim().toUpperCase();

        Integer quantity = inventory.get(normalizedPartNumber);

        if (quantity == null) {
            System.out.println("Tool result: Product not found in inventory.");
            System.out.println("==============================================");

            return """
                    Part Number: %s
                    Inventory Status: NOT_FOUND
                    Quantity: 0
                    """.formatted(normalizedPartNumber);
        }

        String status;

        if (quantity == 0) {
            status = "OUT_OF_STOCK";
        } else if (quantity <= 5) {
            status = "LOW_STOCK";
        } else {
            status = "IN_STOCK";
        }

        System.out.println("Tool result:");
        System.out.println("Part Number: " + normalizedPartNumber);
        System.out.println("Inventory Status: " + status);
        System.out.println("Quantity: " + quantity);
        System.out.println("==============================================");

        return """
                Part Number: %s
                Inventory Status: %s
                Quantity: %d
                """.formatted(
                normalizedPartNumber,
                status,
                quantity
        );
    }
}