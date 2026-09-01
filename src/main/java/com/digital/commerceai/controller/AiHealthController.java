package com.digital.commerceai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller that provides health and availability information
 * for the Commerce AI Assistant application.
 */
@RestController
@Tag(
        name = "Health",
        description = "Application health and availability APIs"
)
public class AiHealthController {

    /**
     * Returns the current application health message.
     *
     * @return application running status message
     */
    @Operation(
            summary = "Check application health",
            description = "Checks whether the Commerce AI Assistant application is running."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Application is running successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Application health check failed",
                    content = @Content
            )
    })
    @GetMapping("/api/ai/health")
    public String health() {
        return "Commerce AI Assistant is running";
    }
}