/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalOverrideRequest {
	private Long appId;
	private Long sessionId;
	private String lokSabhaNo;
	private String lokSabhaSession;
	private String rajyaSabhaNo;
	private LocalDate documentDate;
	private String dsp2Key;
	private String note;
	private Long receiverRoleId;
	private Integer receiverUserId;
	private List<ProcessStepwiseDocumentsDTO> document;
}
