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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionBulletin2DTO {

	private String applicationName;
	private String house;
	private String rajyaSabhaSessionNo;
	private Long documentTypeId;
	private LocalDate documentDate;
    @AllowHtml
	private String inputContent;
    @AllowHtml
    private String translatedContent;
	private String subject;
	private String officeName; // replaced officeId
	private Integer bulletinNo;
}