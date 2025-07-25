/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.dto;

public interface InProgressCountResponse {
	String getStatus(); // must match alias in query

	Long getCount(); // must match alias in query
}
