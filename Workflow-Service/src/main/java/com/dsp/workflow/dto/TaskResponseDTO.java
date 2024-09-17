/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.dto;

import java.util.Date;

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
	private String assignee;
	private Date createTime;

	// Public constructor to make it accessible
	public TaskResponseDTO(String taskId, String taskName, String assignee, Date createTime) {
		this.taskId = taskId;
		this.taskName = taskName;
		this.assignee = assignee;
		this.createTime = createTime;
	}

}
