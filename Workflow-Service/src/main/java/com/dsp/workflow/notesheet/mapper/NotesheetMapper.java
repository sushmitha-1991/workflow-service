/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.notesheet.mapper;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.dsp.workflow.notesheet.entity.Notesheet;

@Component
public class NotesheetMapper {

	/**
	 * Maps the variables from a Map to a Notesheet entity using the builder
	 * pattern.
	 * 
	 * @param variables A Map containing the variables to be mapped.
	 * @return A Notesheet entity.
	 */
	public static Notesheet fromVariables(Map<String, Object> variables, String taskId) {
		return Notesheet.builder().note((String) variables.get("note"))
				.receiverRoleId(convertToInteger(variables.get("receiverRoleId")))
				.receiverUserId((Integer) variables.get("receiverUserId"))
				.senderRoleId(convertToInteger(variables.get("senderRoleId")))
				// .applicationHistoryId((Long) variables.get("applicationInstanceId"))
				.applicationInstanceId((Long) variables.get("applicationInstanceId"))
				.actionTaken((String) variables.get("actionTaken")).build();
	}

	private static Integer convertToInteger(Object value) {
		if (value instanceof Integer) {
			return (Integer) value;
		} else if (value instanceof Long) {
			return ((Long) value).intValue(); // Convert Long to Integer
		} else {
			throw new IllegalArgumentException("Expected Integer or Long, but got: " + value.getClass());
		}
	}
}
