package com.digital.commerceai.rag.model;

public class SemanticProductResponse {

    private String partNumber;
    private String catentryId;
    private String name;
    private String manufacturer;
    private String shortDescription;

    public SemanticProductResponse() {
    }

    public SemanticProductResponse(
            String partNumber,
            String catentryId,
            String name,
            String manufacturer,
            String shortDescription) {

        this.partNumber = partNumber;
        this.catentryId = catentryId;
        this.name = name;
        this.manufacturer = manufacturer;
        this.shortDescription = shortDescription;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getCatentryId() {
        return catentryId;
    }

    public void setCatentryId(String catentryId) {
        this.catentryId = catentryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }
}