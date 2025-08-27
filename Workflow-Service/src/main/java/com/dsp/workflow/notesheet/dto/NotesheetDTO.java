/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.notesheet.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.dsp.workflow.config.XSSConfig.AllowHtml;
import com.dsp.workflow.dto.ProcessStepwiseDocumentsDTO;
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

	private Long id; // Add this field
	@AllowHtml
	private String note;
	private Long receiverRoleId;
	private Integer receiverUserId;
	private String receiverUserName;
	private long senderRoleId;
	private long applicationHistoryId;
	private long applicationInstanceId;
	private String actionTaken;
	private Map<String, Object> additionalDetails;
	private String createdByName;
	private String roleName;
	private LocalDateTime createdAt;
	private String referenceNo;

	@JsonIgnore
	private Long createdBy;
	private List<ProcessStepwiseDocumentsDTO> documents;

	// Constructor to create DTO from Entity
	public NotesheetDTO(Notesheet notesheet) {
		this.id = notesheet.getId(); // Set the id
		this.note = notesheet.getNote();
		this.receiverRoleId = notesheet.getReceiverRoleId();
		this.receiverUserId = notesheet.getReceiverUserId();
		this.senderRoleId = notesheet.getSenderRoleId();
		this.applicationHistoryId = notesheet.getApplicationHistoryId();
		this.applicationInstanceId = notesheet.getApplicationInstanceId();
		this.actionTaken = notesheet.getActionTaken();
		this.additionalDetails = notesheet.getAdditionalDetails();
		this.createdAt = notesheet.getCreatedAt();
		this.createdBy = notesheet.getCreatedBy();

		// Convert NoteStepwiseDocuments to ProcessStepwiseDocumentsDTO
		if (notesheet.getDocuments() != null) {
			this.documents = notesheet.getDocuments().stream().map(ProcessStepwiseDocumentsDTO::new)
					.collect(Collectors.toList());
		}
	}
}
