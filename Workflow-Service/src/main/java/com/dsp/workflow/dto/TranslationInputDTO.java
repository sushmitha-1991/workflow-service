/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.dsp.workflow.config.XSSConfig.AllowHtml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranslationInputDTO {
	private Long lokSabhaNo;
	private Long lokSabhaSessionNo;
	private Long rajyaSabhaSessionNo;
	private Long documentTypeId;
	private LocalDate documentDate;
	@AllowHtml
	private String inputContent;
	private Long startingPageNo;
	private UUID contentUUID;
	private String dsp2key; // for OnlyOffice

	//
	private Boolean translationFlag;

}
