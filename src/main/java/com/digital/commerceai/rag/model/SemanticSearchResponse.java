package com.digital.commerceai.rag.model;

import java.util.ArrayList;
import java.util.List;

public class SemanticSearchResponse {

    private String query;
    private String answer;
    private String searchType;
    private boolean semanticMatchesFound;
    private List<String> partNumbers = new ArrayList<>();
    private List<String> catentryIds = new ArrayList<>();
    private List<SemanticProductResponse> products = new ArrayList<>();

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public boolean isSemanticMatchesFound() {
        return semanticMatchesFound;
    }

    public void setSemanticMatchesFound(boolean semanticMatchesFound) {
        this.semanticMatchesFound = semanticMatchesFound;
    }

    public List<String> getPartNumbers() {
        return partNumbers;
    }

    public void setPartNumbers(List<String> partNumbers) {
        this.partNumbers = partNumbers;
    }

    public List<String> getCatentryIds() {
        return catentryIds;
    }

    public void setCatentryIds(List<String> catentryIds) {
        this.catentryIds = catentryIds;
    }

    public List<SemanticProductResponse> getProducts() {
        return products;
    }

    public void setProducts(List<SemanticProductResponse> products) {
        this.products = products;
    }
}