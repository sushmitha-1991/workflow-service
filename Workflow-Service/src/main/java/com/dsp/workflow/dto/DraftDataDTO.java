/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.dsp.workflow.config.XSSConfig.AllowHtml;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 */
@Data
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DraftDataDTO {
	private Long draftId;
	private JsonNode data;
	private Long applicationId;
	private Long rajyaSabhaSession;
	private Long lokSabhaSessionNumber;
	private Long lokSabhaSession;
	@AllowHtml
	private String actionUrl;
	private String applicationNo;
	private Long createdById;
    private LocalDateTime createdAt;
    private LocalDate createdAtWithoutTime;
    private String createdBy;
    private LocalDate forDate;

}
