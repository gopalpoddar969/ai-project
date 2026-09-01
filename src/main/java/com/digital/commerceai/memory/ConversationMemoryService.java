package com.digital.commerceai.memory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service responsible for storing, retrieving, and clearing conversational memory in Redis.
 */
@Service
public class ConversationMemoryService {

    private static final String KEY_PREFIX = "commerce-ai:conversation:";

    /*
     * Keep conversation memory for 24 hours after the
     * most recent interaction.
     */
    private static final Duration MEMORY_TTL = Duration.ofHours(24);

    /*
     * Maximum number of messages kept in one conversation.
     *
     * 10 messages = approximately 5 user/assistant turns.
     */
    private static final long MAX_MESSAGES = 10;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Creates the conversation memory service with Redis and JSON serialization support.
     *
     * @param redisTemplate Redis template used to store conversation messages
     * @param objectMapper object mapper used to serialize and deserialize messages
     */
    public ConversationMemoryService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Adds a message to the specified conversation and refreshes its expiration time.
     *
     * @param conversationId unique conversation identifier
     * @param role message role such as USER or ASSISTANT
     * @param content message content
     */
    public void addMessage(String conversationId, String role, String content) {
        if (conversationId == null || conversationId.isBlank() || content == null || content.isBlank()) {
            return;
        }

        String key = buildKey(conversationId);
        ConversationMessage message = new ConversationMessage(role, content);

        try {
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.opsForList().rightPush(key, json);

            /*
             * Keep only the latest messages.
             */
            Long size = redisTemplate.opsForList().size(key);

            if (size != null && size > MAX_MESSAGES) {
                redisTemplate.opsForList().trim(key, size - MAX_MESSAGES, -1);
            }

            /*
             * Refresh TTL after every message.
             */
            redisTemplate.expire(key, MEMORY_TTL);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to store conversation message", e);
        }
    }

    /**
     * Fetches all stored messages for a conversation.
     *
     * @param conversationId unique conversation identifier
     * @return list of stored conversation messages
     */
    public List<ConversationMessage> getMessages(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return Collections.emptyList();
        }

        String key = buildKey(conversationId);
        List<String> jsonMessages = redisTemplate.opsForList().range(key, 0, -1);

        if (jsonMessages == null || jsonMessages.isEmpty()) {
            return Collections.emptyList();
        }

        List<ConversationMessage> messages = new ArrayList<>();

        for (String json : jsonMessages) {
            try {
                ConversationMessage message = objectMapper.readValue(json, ConversationMessage.class);
                messages.add(message);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Unable to read conversation message", e);
            }
        }

        return messages;
    }

    /**
     * Converts stored conversation messages into text that can be supplied to the LLM.
     *
     * @param conversationId unique conversation identifier
     * @return formatted conversation history
     */
    public String buildHistory(String conversationId) {
        List<ConversationMessage> messages = getMessages(conversationId);

        if (messages.isEmpty()) {
            return "No previous conversation exists.";
        }

        StringBuilder history = new StringBuilder();

        for (ConversationMessage message : messages) {
            history.append(message.getRole());
            history.append(": ");
            history.append(message.getContent());
            history.append("\n\n");
        }

        return history.toString().trim();
    }

    /**
     * Deletes the complete conversation from Redis.
     *
     * @param conversationId unique conversation identifier
     */
    public void clearConversation(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return;
        }

        redisTemplate.delete(buildKey(conversationId));
    }

    /**
     * Builds the Redis key for a conversation.
     *
     * @param conversationId unique conversation identifier
     * @return Redis key used for the conversation
     */
    private String buildKey(String conversationId) {
        return KEY_PREFIX + conversationId;
    }
}

/**
 * Provides application-wide Jackson JSON serialization support.
 */
@Configuration
class JacksonConfiguration {

    /**
     * Creates the Jackson object mapper used for JSON serialization and deserialization.
     *
     * @return configured object mapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}