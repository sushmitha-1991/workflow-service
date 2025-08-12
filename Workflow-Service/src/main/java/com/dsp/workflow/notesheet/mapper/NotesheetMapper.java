package com.dsp.workflow.notesheet.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.dsp.workflow.dto.ProcessStepwiseDocumentsDTO;
import com.dsp.workflow.entity.NoteStepwiseDocuments;
import com.dsp.workflow.notesheet.entity.Notesheet;

@Component
public class NotesheetMapper {

	/**
	 * Maps the variables from a Map to a Notesheet entity using the builder
	 * pattern, including associated documents.
	 * 
	 * @param variables    A Map containing the variables to be mapped.
	 * @param taskId       The task ID related to the workflow.
	 * @param documentList
	 * @param note
	 * @return A Notesheet entity.
	 */
	public static Notesheet fromVariables(Map<String, Object> variables, String taskId,
			List<ProcessStepwiseDocumentsDTO> documentList, String note) {
		System.out.println(">>> checkpoint 6");

		// Build the Notesheet entity from the variables
		Notesheet notesheet = Notesheet.builder().note(note)
				.receiverRoleId(convertToInteger(variables.get("receiverRoleId")))
				.receiverUserId((Integer) variables.get("receiverUserId"))
				// .senderRoleId(convertToInteger(variables.get("senderRoleId")))
				.applicationInstanceId((Long) variables.get("applicationInstanceId"))
				.actionTaken((String) variables.get("actionTaken")).build();
		System.out.println(">>> checkpoint 7");

		// Initialize the list to hold NoteStepwiseDocuments entities
		List<NoteStepwiseDocuments> noteStepwiseDocumentsList = new ArrayList<>();
		System.out.println(">>> checkpoint 8");

		// Process the documentList (from ProcessStepwiseDocumentsDTO)
		if (documentList != null) {
			for (ProcessStepwiseDocumentsDTO processDocument : documentList) {
				System.out.println(">>> checkpoint 9");

				// Create a new NoteStepwiseDocuments entity for each DTO in the list
				NoteStepwiseDocuments document = NoteStepwiseDocuments.builder()
						.documentName(processDocument.getDocumentName()).documentUuid(processDocument.getDocumentUuid())
						.version(processDocument.getVersion()).notesheet(notesheet) // Associate with the
																					// Notesheet
						.build();
				noteStepwiseDocumentsList.add(document);
			}
		}

		System.out.println(">>> checkpoint 10");

		// Set the associated documents in the Notesheet entity
		notesheet.setDocuments(noteStepwiseDocumentsList);

		return notesheet;
	}

	// Helper method to convert Object to Integer
	private static Integer convertToInteger(Object value) {
		if (value instanceof Integer) {
			return (Integer) value;
		} else if (value instanceof Long) {
			return ((Long) value).intValue(); // Convert Long to Integer
		} else {
			throw new IllegalArgumentException("Expected Integer or Long, but got: " + value.getClass());
		}
	}
}
