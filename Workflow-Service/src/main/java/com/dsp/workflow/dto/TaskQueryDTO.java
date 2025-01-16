/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.dto;

import java.time.LocalDate;

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
	LocalDate fromDate;
    LocalDate toDate;
}