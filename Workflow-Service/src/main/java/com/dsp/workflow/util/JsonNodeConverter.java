/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.util;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class JsonNodeConverter implements AttributeConverter<JsonNode, String> {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(JsonNode attribute) {
		if (attribute == null) {
			return null;
		}
		return attribute.toString(); // Convert JsonNode to String (JSON format)
	}

	@Override
	public JsonNode convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isEmpty()) {
			return null;
		}
		try {
			return objectMapper.readTree(dbData); // Convert String (JSON) back to JsonNode
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to convert String to JsonNode", e);
		}
	}
}
