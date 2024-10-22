package com.dsp.workflow.levelbyabbraction.entity;

import com.dsp.workflow.entity.ApplicationForms;
import com.dsp.workflow.entity.Auditable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "levelbyabbraction", schema = "workflow")
public class LevelByAbbrAction extends Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workflow.levelbyabbraction_seq_gen")
	@SequenceGenerator(name = "levelbyabbraction_seq_gen", sequenceName = "levelbyabbraction_seq", allocationSize = 1)
	private Long id;

	@Column(name = "role_id", nullable = false)
	private Long roleId;

	@Column(name = "level", length = 50, nullable = false)
	private String level;

	@Column(name = "action_key", length = 100, nullable = false)
	private String actionKey;

	@Column(name = "abbr", length = 50, nullable = false)
	private String abbr;

	@Column(name = "step_type", length = 20, nullable = false)
	private String stepType;

	@Column(name = "status", length = 50)
	private String status;

	@Column(name = "status_key", length = 50, nullable = false)
	private String statusKey;

	@Column(name = "application_id", nullable = false)
	private Long applicationId;

	@OneToOne
	@JoinColumn(name = "form_id", referencedColumnName = "id", nullable = false)
	private ApplicationForms applicationForm;

	@Column(name = "workflow_id", length = 50, nullable = false)
	private String workflowId;

	@Column(name = "send_to", length = 20, nullable = false)
	private String sendTo;
}

// get actions by application ID
// find by ID on Action table
