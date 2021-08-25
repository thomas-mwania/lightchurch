package org.lightchurch.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.lightchurch.domain.Config;

/**
 * @author tom9b
 * @version 1.0.0
 * @Date 7/12/2021 Date file was Created
 * @package com.kyosk.configsapi.controllers.vm
 * @project configs-api
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "metadata" })
public class ConfigDTO {

    @JsonProperty("name")
    private String name;

    @JsonProperty("metadata")
    private Map<String, Object> metaData;

    @SuppressWarnings("unchecked")
    public ConfigDTO(Config config) {
        this.name = config.getName();
        try {
            this.metaData = new ObjectMapper().readValue(config.getMetaData(), Map.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public ConfigDTO() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, Object> metaData) {
        this.metaData = metaData;
    }
}
