/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dsp.workflow.dto.ProcessStepwiseDocumentsDTO;
import com.dsp.workflow.response.GenericResponse;
import com.dsp.workflow.service.ProcessStepwiseDocumentsService;

@RestController
@RequestMapping("/workflow/process-stepwise-documents")
public class NoteStepwiseDocumentsController {

	private final ProcessStepwiseDocumentsService documentsService;

	public NoteStepwiseDocumentsController(ProcessStepwiseDocumentsService documentsService) {
		this.documentsService = documentsService;
	}

	@PostMapping("/create")
	public ResponseEntity<GenericResponse<ProcessStepwiseDocumentsDTO>> createDocument(
			@RequestBody ProcessStepwiseDocumentsDTO documentDTO) {
		ProcessStepwiseDocumentsDTO createdDocument = documentsService.createDocument(documentDTO);
		return new ResponseEntity<>(new GenericResponse<>(true, "Document created successfully", createdDocument,
				HttpStatus.CREATED.value()), HttpStatus.CREATED);
	}

	@GetMapping
	public ResponseEntity<GenericResponse<List<ProcessStepwiseDocumentsDTO>>> getAllDocuments() {
		List<ProcessStepwiseDocumentsDTO> documents = documentsService.getAllDocuments();
		return new ResponseEntity<>(
				new GenericResponse<>(true, "All documents retrieved successfully", documents, HttpStatus.OK.value()),
				HttpStatus.OK);
	}

	@GetMapping("/{id}")
	public ResponseEntity<GenericResponse<ProcessStepwiseDocumentsDTO>> getDocumentById(@PathVariable Long id) {
		ProcessStepwiseDocumentsDTO document = documentsService.getDocumentById(id);
		return new ResponseEntity<>(
				new GenericResponse<>(true, "Document retrieved successfully", document, HttpStatus.OK.value()),
				HttpStatus.OK);
	}

	@PutMapping("/{id}")
	public ResponseEntity<GenericResponse<ProcessStepwiseDocumentsDTO>> updateDocument(@PathVariable Long id,
			@RequestBody ProcessStepwiseDocumentsDTO documentDTO) {
		ProcessStepwiseDocumentsDTO updatedDocument = documentsService.updateDocument(id, documentDTO);
		return new ResponseEntity<>(
				new GenericResponse<>(true, "Document updated successfully", updatedDocument, HttpStatus.OK.value()),
				HttpStatus.OK);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<GenericResponse<Void>> deleteDocument(@PathVariable Long id) {
		documentsService.deleteDocument(id);
		return new ResponseEntity<>(
				new GenericResponse<>(true, "Document deleted successfully", null, HttpStatus.NO_CONTENT.value()),
				HttpStatus.NO_CONTENT);
	}
}
