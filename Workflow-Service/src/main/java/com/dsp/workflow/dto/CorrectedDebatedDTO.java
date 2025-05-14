/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.dto;

import java.time.LocalDate;

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

	private Long loksbhano;

	private Long loksbhanosession;

	private Long rajyasbhanosession;

	private Long documentTypeId;

	private LocalDate documentDate;

	private LocalDate receivedDate;

	private String inputContent;

	private String contentUuid;
}
