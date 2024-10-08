/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.dto;

/**
 * 
 */
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskResponseDTO {

	private String taskId;
	private String taskName;

	// Public constructor to make it accessible
	public TaskResponseDTO(String taskId, String taskName) {
		this.taskId = taskId;
		this.taskName = taskName;

	}

}
