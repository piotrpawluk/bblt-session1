package com.drfirst.bblt.session1.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RecipeRequest {
    
    @JsonProperty("ingredients")
    private String ingredients;
    
    @JsonProperty("cuisine")
    private String cuisine;
    
    @JsonProperty("dietaryRestrictions")
    private String dietaryRestrictions;
    
    @JsonProperty("modelId")
    private String modelId;

    public RecipeRequest() {}

    public RecipeRequest(String ingredients, String cuisine, String dietaryRestrictions, String modelId) {
        this.ingredients = ingredients;
        this.cuisine = cuisine;
        this.dietaryRestrictions = dietaryRestrictions;
        this.modelId = modelId;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getCuisine() {
        return cuisine != null ? cuisine : "Italian";
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public String getDietaryRestrictions() {
        return dietaryRestrictions != null ? dietaryRestrictions : "";
    }

    public void setDietaryRestrictions(String dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }

    public String getModelId() {
        return modelId != null ? modelId : "claude-3-7-sonnet";
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }
}