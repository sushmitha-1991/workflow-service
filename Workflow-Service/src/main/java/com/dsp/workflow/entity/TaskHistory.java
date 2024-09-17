/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "workflow")
public class TaskHistory extends Auditable {

	@Id
	@SequenceGenerator(name = "task_history_seq", sequenceName = "task_history_sequence", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_history_seq")
	private Long id;

	private String taskId;

	private String processInstanceId;

	private String userId;

	private String userName;

	private String action;

	private String comments;


	private Date timestamp;

	private String status;

	private String nextTaskId;

	private String assignee;

	private String owner;

	// If needed, you can uncomment these lines for documentTitle and docDesc
	// private String documentTitle;
	// private String docDescription;
}
