/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.notesheet.mapper;

import java.util.Map;

import com.dsp.workflow.entity.ApplicationHistory;

public class ApplicationHistoryMapper {

	public static ApplicationHistory fromVariables(Map<String, Object> variables, String taskId) {
		return ApplicationHistory.builder()
				.applicationInstanceId(((Number) variables.get("applicationInstanceId")).longValue()) // Convert Integer
																										// to Long
				.applicationNo((String) variables.get("application_no")) // Application number as a string
				.status("Completed") // Set the status appropriately; you can change this logic as per your
										// requirement
				.isCurrent(true) // Assuming this is always set to true; adjust as necessary
				.taskId(taskId).additionalDetails((Map<String, Object>) variables.get("additional_details"))// Task ID
																											// as
																											// string
				.build();
	}
}
