package com.digital.commerceai.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.digital.commerceai.tool.InventoryLookupTool;
import com.digital.commerceai.tool.ProductSearchTool;

/**
 * Configures the Commerce MCP server tool provider.
 *
 * <p>The provider converts the existing Spring AI {@code @Tool}
 * methods from the Commerce product and inventory tools into
 * Spring AI {@link ToolCallback} instances.</p>
 *
 * <p>The same underlying business methods are therefore available
 * both to the existing Spring AI tool-calling flow and to external
 * MCP clients.</p>
 */
@Configuration
public class McpServerConfig {

    /**
     * Creates the tool callback provider used by the MCP server.
     *
     * <p>The existing product search and inventory tool objects are
     * registered without duplicating their business logic.</p>
     *
     * @param productSearchTool existing product search tool
     * @param inventoryLookupTool existing inventory lookup tool
     * @return tool callback provider containing the Commerce tools
     */
    @Bean
    public ToolCallbackProvider commerceMcpTools(
            ProductSearchTool productSearchTool,
            InventoryLookupTool inventoryLookupTool) {

        return MethodToolCallbackProvider.builder()
                .toolObjects(
                        productSearchTool,
                        inventoryLookupTool
                )
                .build();
    }
}