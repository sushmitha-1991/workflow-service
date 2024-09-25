/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.dsp.workflow.response.GenericResponse;

/**
 * 
 */
public class GlobalExceptionHandler {
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<GenericResponse<String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
		GenericResponse<String> response = new GenericResponse<>(false, "Validation failed", errors.toString(),
				HttpStatus.BAD_REQUEST.value());
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<GenericResponse<String>> handleUserNotFoundException(UserNotFoundException ex) {
		GenericResponse<String> response = new GenericResponse<>(false, ex.getMessage(), null,
				HttpStatus.NOT_FOUND.value());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<GenericResponse<String>> handleIllegalArgumentException(IllegalArgumentException ex) {
		GenericResponse<String> response = new GenericResponse<>(false, ex.getMessage(), null,
				HttpStatus.BAD_REQUEST.value());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<GenericResponse<String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
		GenericResponse<String> response = new GenericResponse<>(false, ex.getMessage(), null,
				HttpStatus.NOT_FOUND.value());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<GenericResponse<String>> handleGeneralException(Exception ex) {
		GenericResponse<String> response = new GenericResponse<>(false, "An unexpected error occurred", null,
				HttpStatus.INTERNAL_SERVER_ERROR.value());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

}
