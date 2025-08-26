/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.dto;

import java.time.LocalDate;

import com.dsp.workflow.config.XSSConfig.AllowHtml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebateTaggingDTO {

	private Long id;

	@AllowHtml
	private String content;

	private LocalDate debateDate;

	private Long lokSabhaNo;

	private Long lokSabhaSessionNo;

	private Long rajyaSabhaSessionNo;

	private Boolean supplementaryQuestionFlag;

	private Long applicationId;

	private String status;

}
