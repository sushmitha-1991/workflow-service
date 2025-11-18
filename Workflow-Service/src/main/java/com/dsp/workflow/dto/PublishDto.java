/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublishDto {

	private String lokSabhaNo;
	private String lokSabhaSessionNo;
	private String rajyaSabhaSession;
	private String forDate;
	private Long applicationId;
	private UUID docUuidEn;
	private UUID docUuidHi;

}
