/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.notesheet.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.dsp.workflow.notesheet.entity.Notesheet;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotesheetDTO {

	private String note;
	private long receiverRoleId;
	private int receiverUserId;
	private long senderRoleId;
	private long applicationHistoryId;
	private long applicationInstanceId;
	private String actionTaken;
	private Map<String, Object> additionalDetails;
	private String createdByName; // Creator's full name
	private String roleName; // Role name (abbr + role_name)
	private LocalDateTime createdAt;

	@JsonIgnore // Exclude createdBy from JSON serialization
	private Long createdBy;

	// Constructor to create DTO from Entity
	public NotesheetDTO(Notesheet notesheet) {
		this.note = notesheet.getNote();
		this.receiverRoleId = notesheet.getReceiverRoleId();
		this.receiverUserId = notesheet.getReceiverUserId();
		this.senderRoleId = notesheet.getSenderRoleId();
		this.applicationHistoryId = notesheet.getApplicationHistoryId();
		this.applicationInstanceId = notesheet.getApplicationInstanceId();
		this.actionTaken = notesheet.getActionTaken();
		this.additionalDetails = notesheet.getAdditionalDetails();
		this.createdAt = notesheet.getCreatedAt();
		this.createdBy = notesheet.getCreatedBy(); // Will not be serialized in response
	}
}
