/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.dto;

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
	private String applicationNo;
    

}
