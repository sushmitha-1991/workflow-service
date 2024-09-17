/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.notesheet.mapper;

import java.util.Map;

import com.dsp.workflow.notesheet.entity.Notesheet;

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
				.receiverRoleId((Integer) variables.get("receiverRoleId"))
				.receiverUserId((Integer) variables.get("receiverUserId"))
				.senderRoleId((Integer) variables.get("senderRoleId")).historyId(taskId)
				.applicationInstanceId((Integer) variables.get("applicationInstanceId"))
				.actionTaken((String) variables.get("actionTaken")).build();
	}
}
