package com.dsp.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProcessStepwiseDocumentsDTO {

	private Long id;
	private String documentName;
	private String documentUuid;
	private String uploadedBy;
	private Long noteSheetId; // Representing the relation to WorkflowHistory

}
