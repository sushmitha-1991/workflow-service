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
public class CorrectedDebatedDTO {
	private Long id;

	private Long lokSabhaNo;

	private Long lokSabhaSessionNo;

	private Long rajyaSabhaSessionNo;

	private Long documentTypeId;

	private LocalDate documentDate;

	private LocalDate receivedDate;

	@AllowHtml
	private String inputContent;

	private String contentUUID;
}
