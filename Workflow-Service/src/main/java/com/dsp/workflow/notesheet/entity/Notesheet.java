/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.notesheet.entity;

import com.dsp.workflow.entity.Auditable;

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
@Table(schema = "workflow")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notesheet extends Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notesheet_seq")
	@SequenceGenerator(name = "notesheet_seq", sequenceName = "workflow.notesheet_seq", allocationSize = 1)
	private Long id;

	@Column(nullable = false)
	private String note;

	@Column(name = "receiver_role_id", nullable = false)
	private int receiverRoleId;

	@Column(name = "receiver_user_id", nullable = false)
	private int receiverUserId;

	@Column(name = "sender_role_id", nullable = false)
	private int senderRoleId;

	@Column(name = "history_id", nullable = false)
	private String historyId;

	@Column(name = "application_instance_id", nullable = false)
	private int applicationInstanceId;

	@Column(name = "action_taken", length = 255)
	private String actionTaken;

}
