//package com.dsp.workflow.util;
//
//import java.io.IOException;
//
//import com.dsp.workflow.dto.CompleteTaskRequestDTO.ReturnToType;
//import com.fasterxml.jackson.core.JsonParser;
//import com.fasterxml.jackson.databind.DeserializationContext;
//import com.fasterxml.jackson.databind.JsonDeserializer;
//
//public class ReturnToTypeDeserializer extends JsonDeserializer<ReturnToType> {
//	@Override
//	public ReturnToType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
//		String value = p.getText();
//		if (value == null || value.isEmpty()) {
//			return null; // Treat empty string as null
//		}
//		return ReturnToType.valueOf(value.toUpperCase());
//	}
//}
