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
@Table(name = "application_authority", schema = "workflow")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationAuthority extends Auditable {

	@Id
	@SequenceGenerator(name = "application_authority_seq", sequenceName = "workflow.application_authority_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "application_authority_seq")
	private Long id;

	@Column(name = "application_history_id", nullable = false)
	private Long applicationHistoryId;

	@Column(name = "application_instance_id", nullable = false)
	private Long applicationInstanceId;

	@Column(name = "office_id", nullable = false)
	private Long officeId;

	@Column(name = "role_id", nullable = false)
	private Long roleId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "action_status", length = 100, nullable = false)
	private String actionStatus;

}
