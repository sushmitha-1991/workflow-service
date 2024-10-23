/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.exception;

/**
 * 
 */
public class CamundaException extends RuntimeException {
	public CamundaException(String message, Exception e) {
		super(message, e);
	}
}