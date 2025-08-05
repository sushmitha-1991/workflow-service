package com.dsp.workflow.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
    import java.util.List;
    import java.util.Map;
    
    @Converter
    public class JsonConverter implements AttributeConverter<List<Map<String, Object>>, String> {

    // private final ObjectMapper objectMapper = new ObjectMapper();

    ObjectMapper objectMapper = new ObjectMapper();
 
    @Override
    public String convertToDatabaseColumn(List<Map<String, Object>> attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to convert list of maps to JSON string: " + attribute, e);
        }
    }

    @Override
    public List<Map<String, Object>> convertToEntityAttribute(String dbData) {
        System.out.println("jjjjjjjjjjjjj");
        System.out.println(dbData);
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, List.class);
        } catch (JsonMappingException e) {
            throw new IllegalArgumentException("Failed to map JSON string to list of maps: " + dbData, e);
        } catch (IOException e) {
            throw new IllegalArgumentException("I/O error while reading JSON string: " + dbData, e);
        }
    }

}
    