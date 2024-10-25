/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.mapper;

import org.springframework.stereotype.Component;

import com.dsp.workflow.dto.ProcessStepwiseDocumentsDTO;
import com.dsp.workflow.entity.NoteStepwiseDocuments;
import com.dsp.workflow.notesheet.entity.Notesheet;

@Component
public class NotesheetStepwiseDocumentsMapper {

	public ProcessStepwiseDocumentsDTO toDTO(NoteStepwiseDocuments entity) {
		return ProcessStepwiseDocumentsDTO.builder().id(entity.getId()).documentName(entity.getDocumentName())
				.documentUuid(entity.getDocumentUuid()).build();
	}

	public NoteStepwiseDocuments toEntity(ProcessStepwiseDocumentsDTO dto, Notesheet notesheet) {
		// Creating the entity with the passed notesheet
		return NoteStepwiseDocuments.builder().documentName(dto.getDocumentName()).documentUuid(dto.getDocumentUuid())
				.notesheet(notesheet) // Set the notesheet here
				.build();
	}

	public void updateEntityFromDTO(ProcessStepwiseDocumentsDTO dto, NoteStepwiseDocuments entity,
			Notesheet notesheet) {
		entity.setDocumentName(dto.getDocumentName());
		entity.setDocumentUuid(dto.getDocumentUuid());
		entity.setNotesheet(notesheet); // Update the notesheet as well
	}
}
