package com.digital.commerceai.rag;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.digital.commerceai.rag.config.SolrProperties;
import com.digital.commerceai.rag.model.SolrDocument;
import com.digital.commerceai.rag.model.SolrResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class KnowledgeIngestionService implements CommandLineRunner {

    public enum IngestionStatus {
        STARTING,
        INGESTING,
        READY,
        FAILED
    }
    private final VectorStore vectorStore;
    private final RestClient restClient;
    private final SolrProperties solrProperties;
    private volatile IngestionStatus status = IngestionStatus.STARTING;

    public KnowledgeIngestionService(VectorStore vectorStore, SolrProperties solrProperties) {
        this.vectorStore = vectorStore;
        this.restClient = RestClient.builder().build();
        this.solrProperties = solrProperties;
    }

    @Override
    public void run(String... args) {
        try {
            status = IngestionStatus.INGESTING;
            loadKnowledge();
            status = IngestionStatus.READY;
        } catch (Exception e) {
            status = IngestionStatus.FAILED;
            e.printStackTrace();
        }
    }

    public boolean isReady() {
        return status == IngestionStatus.READY;
    }

    public IngestionStatus getStatus() {
        return status;
    }

    public void loadKnowledge() throws Exception {
        String responseBody = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host(solrProperties.getHost())
                        .port(solrProperties.getPort())
                        .path(solrProperties.getPath())
                        .queryParam("q", "*:*")
                        .queryParam("rows", "10")
                        .queryParam("wt", "json")
                        .build())
                .headers(headers ->
                        headers.setBasicAuth(
                                solrProperties.getUsername(),
                                solrProperties.getPassword()))
                .retrieve()
                .body(String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        SolrResponse response = objectMapper.readValue(responseBody, SolrResponse.class);
        if (response == null
                || response.getResponse() == null
                || response.getResponse().getDocs() == null
                || response.getResponse().getDocs().isEmpty()) {
            return;
        }
        List<Document> documents = new ArrayList<>();
        for (SolrDocument solrDocument : response.getResponse().getDocs()) {
            Document document = convertToVectorDocument(solrDocument);
            if (document != null) {
                documents.add(document);
            }
        }
        if (!documents.isEmpty()) {
            vectorStore.add(documents);
        }
    }

    private Document convertToVectorDocument(SolrDocument solrDocument) {
        if (solrDocument == null || solrDocument.getCatentryId() == null) {
            return null;
        }
        String content = buildSemanticContent(solrDocument);
        return new Document(content);
    }

    private String buildSemanticContent(SolrDocument product) {
        StringBuilder content = new StringBuilder();
        append(content, "Catentry ID", product.getCatentryId());
        append(content, "Product name", product.getName());
        append(content, "Part number", product.getPartNumber());
        append(content, "Manufacturer", product.getManufacturerName());
        append(content, "Manufacturer part number", product.getManufacturerPartNumber());
        append(content, "Short description", product.getShortDescription());
        append(content, "Long description", product.getLongDescription());
        append(content, "Product attributes", product.getProductAttributes());
        append(content, "Quantity multiple", product.getQuantityMultiple());
        append(content, "Quantity measure", product.getQuantityMeasure());
        append(content, "Weight", product.getWeight());
        append(content, "Weight measure", product.getWeightMeasure());
        return content.toString();
    }

    private void append(StringBuilder builder, String field, Object value) {
        if (value != null && !value.toString().trim().isEmpty()) {
            builder.append(field)
                    .append(": ")
                    .append(value)
                    .append("\n");
        }
    }
}