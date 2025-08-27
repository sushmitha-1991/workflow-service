/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.entity;

import java.time.LocalDateTime;

import com.dsp.workflow.config.XSSConfig.AllowHtml;
import com.dsp.workflow.config.XSSConfig.NoEscape;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Builder
@Table(schema = "workflow", name = "workflow_history")
public class WorkflowHistory extends Auditable {

	@Id
	@SequenceGenerator(name = "workflow_history_seq", sequenceName = "workflow.workflow_history_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workflow_history_seq")
	private Long id;

	@Column(name = "process_instance_id", nullable = true)
	private String processInstanceId;

	@Column(name = "sub_module_id", nullable = true)
	private String subModuleId;

	@Column(name = "application_instance_id", nullable = true)
	private Long applicationInstanceId;

	@Column(name = "action_taken_by", nullable = true)
	private Long actionTakenBy;

	@Column(name = "action_taken_by_name", nullable = true)
	private String actionTakenByName;

	@Column(name = "action_taken_on", nullable = true)
	private LocalDateTime actionTakenOn;

	@Column(name = "action_taken", nullable = true)
	private String actionTaken;

	@Column(name = "status", nullable = true)
	private String status;

	@Column(name = "status_key", nullable = true)
	private String statusKey;

	@Column(name = "pending_with", nullable = true)
	private Long pendingWith;

	@Column(name = "pending_with_name", nullable = true)
	private String pendingWithName;

	@Column(name = "note", nullable = true, columnDefinition = "text")
	private String note;

	@Column(name = "pending_start_date", nullable = true)
	private LocalDateTime pendingStartDate;

	@Column(name = "inital_status_key", nullable = true)
	private String initalStatusKey;

	@Column(name = "current_task_id", nullable = true)
	private String currentTaskId;

	@Column(name = "next_task_id", nullable = true)
	private String nextTaskId;

	@NoEscape
	@Column(name = "action_url")
	private String actionUrl;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "draft_id", referencedColumnName = "id")
	private DraftDocument draftDocument;

	@Column(name = "translation_success")
	private Boolean translationSuccess;

	@Column(name = "corrected_debate_success")
	private Boolean correctedDebateSuccess;

}
