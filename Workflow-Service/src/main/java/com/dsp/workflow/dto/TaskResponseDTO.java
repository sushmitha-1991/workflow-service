/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)  
public class TaskResponseDTO {
	private String applicationNo; // application_no from application_instance
	private String documentName; // name from master.application
	private Date date; // created_at from application_instance
	private String status; // current_status from application_instance
	private String pendingAt; // First_name + Last_name from base_user
	private String lsNo;
	private String ssNo;
	private Long refId;
	private Long applicationId;
	private String taskId; // Added to store the task ID
	private String taskName; // Added to store the task name
	private Long applicationInstanceId; // Keep this for additional details
	private String processInstanceId;
	private boolean exists; 
}
