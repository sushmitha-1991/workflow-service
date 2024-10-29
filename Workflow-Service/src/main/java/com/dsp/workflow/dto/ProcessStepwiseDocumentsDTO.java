package com.dsp.workflow.dto;

import com.dsp.workflow.entity.NoteStepwiseDocuments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessStepwiseDocumentsDTO {

	private Long id;
	private String documentName;
	private String documentUuid;
	private String uploadedBy;
	private String version;

	// Constructor to map entity to DTO
	public ProcessStepwiseDocumentsDTO(NoteStepwiseDocuments document) {
		this.documentName = document.getDocumentName();
		this.documentUuid = document.getDocumentUuid();
		this.uploadedBy = document.getUploadedBy();
		this.version = document.getVersion();
	}
}
