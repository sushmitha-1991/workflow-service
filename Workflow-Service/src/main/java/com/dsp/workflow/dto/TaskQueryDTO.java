/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskQueryDTO {
	String lsNo;
	String lsSessionNo;
	String rsSessionNo;
	Long applicationId;
	String pendingAt;
}