/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.kafka;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class KafkaProducer {

	private static final String TOPIC = "Dsp2";

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	private final ObjectMapper mapper = new ObjectMapper();

	public void sendNotification(Long userId, Long sentByUserId, String message, String groupType, String sentFrom) {
		try {
			Map<String, Object> payload = new HashMap<>();
			payload.put("userId", userId);
			payload.put("sentByUserId", sentByUserId);
			payload.put("message", message);
			payload.put("groupType", groupType);
			payload.put("sentFrom", sentFrom);

			String jsonMessage = mapper.writeValueAsString(payload);
			System.out.println(">>> notification sending");
			kafkaTemplate.send(TOPIC, jsonMessage);

		} catch (Exception e) {
			// Log or rethrow as needed
			System.out.println("#### exception - " + e);

			e.printStackTrace();
		}
	}
}
