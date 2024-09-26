/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.notesheet.entity;

import java.util.Map;

import com.dsp.workflow.entity.Auditable;
import com.dsp.workflow.util.MapJsonConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
	private long receiverRoleId;

	@Column(name = "receiver_user_id", nullable = false)
	private int receiverUserId;

	@Column(name = "sender_role_id", nullable = false)
	private long senderRoleId;

	@Column(name = "application_history_id", nullable = false)
	private long applicationHistoryId;

	@Column(name = "application_instance_id", nullable = false)
	private long applicationInstanceId;

	@Column(name = "action_taken", length = 255)
	private String actionTaken;

	@Convert(converter = MapJsonConverter.class)
	@Column(name = "additional_details", columnDefinition = "jsonb")
	private Map<String, Object> additionalDetails; // New field to store JSONB data
}
