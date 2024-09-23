/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "application_history", schema = "workflow")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationHistory extends Auditable {

	@Id
	@SequenceGenerator(name = "application_history_seq", sequenceName = "workflow.application_history_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "application_history_seq")
	private Long id;

	@Column(name = "application_instance_id", nullable = false)
	private Long applicationInstanceId;

	@Column(name = "application_no", length = 100, nullable = false)
	private String applicationNo;

	@Column(name = "status", length = 100, nullable = false)
	private String status;

	@Column(name = "is_current", nullable = false)
	private Boolean isCurrent;

	@Column(name = "task_id", length = 100, nullable = false)
	private String taskId;

	@Column(name = "additional_details", columnDefinition = "jsonb", nullable = false)
	private String additionalDetails;

}
