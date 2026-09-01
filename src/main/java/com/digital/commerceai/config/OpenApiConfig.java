package com.digital.commerceai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for customizing the OpenAPI documentation for the Commerce AI Assistant APIs.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates and configures the OpenAPI definition for the Commerce AI Assistant REST APIs.
     *
     * @return configured OpenAPI documentation metadata
     */
    @Bean
    public OpenAPI commerceAiOpenAPI() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("Commerce AI Assistant API")
                                .version("1.0")
                                .description(
                                        """
                                        REST APIs for the Commerce AI Assistant.

                                        This application demonstrates:
                                        - Generative AI
                                        - Claude integration
                                        - RAG
                                        - Semantic product search
                                        - Redis Vector Store
                                        - Conversational memory
                                        - Redis-based chat history
                                        """
                                )
                                .contact(
                                        new Contact()
                                                .name("Commerce AI Assistant")
                                )
                );
    }
}