package com.digital.commerceai.memory;

public class ConversationResponse {

    private String conversationId;
    private String question;
    private String answer;

    public ConversationResponse() {
    }

    public ConversationResponse(String conversationId, String question, String answer) {
        this.conversationId = conversationId;
        this.question = question;
        this.answer = answer;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}