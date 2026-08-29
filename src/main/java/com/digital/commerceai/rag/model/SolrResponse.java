package com.digital.commerceai.rag.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SolrResponse {

    private SolrResponseBody response;

    public SolrResponseBody getResponse() {
        return response;
    }

    public void setResponse(SolrResponseBody response) {
        this.response = response;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SolrResponseBody {
        private long numFound;
        private int start;
        private List<SolrDocument> docs;

        public long getNumFound() {
            return numFound;
        }

        public void setNumFound(long numFound) {
            this.numFound = numFound;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public List<SolrDocument> getDocs() {
            return docs;
        }

        public void setDocs(List<SolrDocument> docs) {
            this.docs = docs;
        }
    }
}