/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.response;

import com.dsp.workflow.config.XSSConfig.NoEscape;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GenericResponse<T> {

	private int statusCode;
	private boolean success;
	@NoEscape
	private String message;
	private T data;

	public GenericResponse(boolean success, String message, T data, int statusCode) {
		this.statusCode = statusCode;
		this.success = success;
		this.message = message;
		this.data = data;
	}
}
