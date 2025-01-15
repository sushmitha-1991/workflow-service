/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.dto;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DraftDataDTO {
	private JsonNode data;
	private Long applicationId;
	private Long rajyaSabhaSession;
	private Long lokSabhaSessionNumber;
	private Long lokSabhaSession;

}
