package com.dsp.workflow.levelbyabbraction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LevelByAbbrActionDTO {

	private Long id;
	private Long roleId;
	private String level;
	private String actionKey;
	private String abbr;
	private String stepType;
	private String status;
	private String statusKey;
	private Long applicationId;
	private String workflowId;
	private String sendTo;

	// Add fields from ApplicationForms
	private Long formId;
	private String formName;
	private String formUrl;
	private String reportUrl;
	private String formAbbr;
}
