/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.notesheet.mapper;

import java.util.Map;

import com.dsp.workflow.entity.ApplicationAuthority;

public class ApplicationAuthorityMapper {

	public static ApplicationAuthority fromVariables(Map<String, Object> variables, Long applicationHistoryId) {
		return ApplicationAuthority.builder().applicationHistoryId(applicationHistoryId) // Assuming
																							// applicationHistoryId is
																							// passed correctly as Long
				.applicationInstanceId(((Number) variables.get("applicationInstanceId")).longValue()) // Convert Integer
																										// to Long
				.officeId(((Number) variables.get("officeId")).longValue()) // Convert Integer to Long
				.roleId(((Number) variables.get("receiverRoleId")).longValue()) // Convert Integer to Long
				.userId(((Number) variables.get("receiverUserId")).longValue()) // Convert Integer to Long
				.actionStatus((String) variables.get("actionTaken")) // String values are directly casted
				.build();
	}
}
