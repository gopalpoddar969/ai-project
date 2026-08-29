package com.digital.commerceai.rag.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SolrDocument {

    @JsonProperty("catentry_id")
    private String catentryId;

    @JsonProperty("partNumber_ntk")
    private String partNumber;

    @JsonProperty("name")
    private String name;

    @JsonProperty("shortDescription")
    private String shortDescription;

    @JsonProperty("longDescription")
    private String longDescription;

    @JsonProperty("mfName")
    private String manufacturerName;

    @JsonProperty("mfPartNumber_ntk")
    private String manufacturerPartNumber;

    @JsonProperty("x_field4_q")
    private List<String> productAttributes;

    @JsonProperty("x_quantityMultiple")
    private String quantityMultiple;

    @JsonProperty("x_quantityMeasure")
    private String quantityMeasure;

    @JsonProperty("x_weight")
    private String weight;

    @JsonProperty("x_weightMeasure")
    private String weightMeasure;

    @JsonProperty("buyable")
    private Integer buyable;

    @JsonProperty("x_markfordelete")
    private Integer markForDelete;

    @JsonProperty("published")
    private Integer published;

    @JsonProperty("state")
    private String state;

    @JsonProperty("catalog_id")
    private List<Long> catalogId;

    public String getCatentryId() {
        return catentryId;
    }

    public void setCatentryId(String catentryId) {
        this.catentryId = catentryId;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getManufacturerPartNumber() {
        return manufacturerPartNumber;
    }

    public void setManufacturerPartNumber(String manufacturerPartNumber) {
        this.manufacturerPartNumber = manufacturerPartNumber;
    }

    public List<String> getProductAttributes() {
        return productAttributes;
    }

    public void setProductAttributes(List<String> productAttributes) {
        this.productAttributes = productAttributes;
    }

    public String getQuantityMultiple() {
        return quantityMultiple;
    }

    public void setQuantityMultiple(String quantityMultiple) {
        this.quantityMultiple = quantityMultiple;
    }

    public String getQuantityMeasure() {
        return quantityMeasure;
    }

    public void setQuantityMeasure(String quantityMeasure) {
        this.quantityMeasure = quantityMeasure;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getWeightMeasure() {
        return weightMeasure;
    }

    public void setWeightMeasure(String weightMeasure) {
        this.weightMeasure = weightMeasure;
    }

    public Integer getBuyable() {
        return buyable;
    }

    public void setBuyable(Integer buyable) {
        this.buyable = buyable;
    }

    public Integer getMarkForDelete() {
        return markForDelete;
    }

    public void setMarkForDelete(Integer markForDelete) {
        this.markForDelete = markForDelete;
    }

    public Integer getPublished() {
        return published;
    }

    public void setPublished(Integer published) {
        this.published = published;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<Long> getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(List<Long> catalogId) {
        this.catalogId = catalogId;
    }
}