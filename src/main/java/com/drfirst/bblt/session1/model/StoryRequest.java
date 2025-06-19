package com.drfirst.bblt.session1.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StoryRequest {
    
    @JsonProperty("theme")
    private String theme;
    
    @JsonProperty("genre")
    private String genre;
    
    @JsonProperty("length")
    private String length;
    
    @JsonProperty("characters")
    private String characters;
    
    @JsonProperty("modelId")
    private String modelId;

    public StoryRequest() {}

    public StoryRequest(String theme, String genre, String length, String characters, String modelId) {
        this.theme = theme;
        this.genre = genre;
        this.length = length;
        this.characters = characters;
        this.modelId = modelId;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getGenre() {
        return genre != null ? genre : "adventure";
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getLength() {
        return length != null ? length : "short";
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getCharacters() {
        return characters != null ? characters : "a brave protagonist";
    }

    public void setCharacters(String characters) {
        this.characters = characters;
    }

    public String getModelId() {
        return modelId != null ? modelId : "claude-3-7-sonnet";
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }
}