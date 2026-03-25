package com.dsp.workflow.notesheet.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dsp.workflow.dto.ProcessStepwiseDocumentsDTO;
import com.dsp.workflow.entity.NoteStepwiseDocuments;
import com.dsp.workflow.mapper.NotesheetStepwiseDocumentsMapper;
import com.dsp.workflow.notesheet.dto.NotesheetDTO;
import com.dsp.workflow.notesheet.entity.Notesheet;
import com.dsp.workflow.notesheet.repository.NotesheetRepository;
import com.dsp.workflow.notesheet.service.NotesheetService;
import com.dsp.workflow.repository.MyUserRepository;

import jakarta.transaction.Transactional;

@Service
public class NotesheetServiceImpl implements NotesheetService {

	@Autowired
	private NotesheetRepository notesheetRepository;

	@Autowired
	private MyUserRepository baseUserRepository;

	@Autowired
	private NotesheetStepwiseDocumentsMapper documentsMapper;

	@Override
	public NotesheetDTO createNotesheet(NotesheetDTO notesheetDTO, Long senderRoleId) {
		// Build the Notesheet entity from the DTO
		Notesheet notesheet = Notesheet.builder().note(notesheetDTO.getNote())
				.receiverRoleId(notesheetDTO.getReceiverRoleId()).receiverUserId(notesheetDTO.getReceiverUserId())
				.senderRoleId(senderRoleId).applicationHistoryId(notesheetDTO.getApplicationHistoryId())
				.applicationInstanceId(notesheetDTO.getApplicationInstanceId())
				.actionTaken(notesheetDTO.getActionTaken()).additionalDetails(notesheetDTO.getAdditionalDetails()) // Proper
																													// mapping
				.build();

		// Save the entity to the repository
		notesheet = notesheetRepository.save(notesheet);

		// Return the saved entity mapped back to the DTO
		return NotesheetDTO.builder().note(notesheet.getNote()).receiverRoleId(notesheet.getReceiverRoleId())
				.receiverUserId(notesheet.getReceiverUserId()).senderRoleId(notesheet.getSenderRoleId())
				.applicationHistoryId(notesheet.getApplicationHistoryId())
				.applicationInstanceId(notesheet.getApplicationInstanceId()).actionTaken(notesheet.getActionTaken())
				.additionalDetails(notesheet.getAdditionalDetails()) // Map back the new field
				.build();
	}

	@Override
	@Transactional
	public NotesheetDTO getNotesheetById(Long id) {
		Notesheet notesheet = notesheetRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Notesheet not found with the given ID"));

		// Fetch associated documents
		List<NoteStepwiseDocuments> associatedDocuments = notesheet.getDocuments();

		// Map associated documents to DTOs
		List<ProcessStepwiseDocumentsDTO> documentDTOs = associatedDocuments.stream().map(documentsMapper::toDTO)
				.collect(Collectors.toList());

		// Build and return the NotesheetDTO including the associated documents
		return NotesheetDTO.builder().note(notesheet.getNote()).receiverRoleId(notesheet.getReceiverRoleId())
				.receiverUserId(notesheet.getReceiverUserId()).senderRoleId(notesheet.getSenderRoleId())
				.applicationHistoryId(notesheet.getApplicationHistoryId())
				.applicationInstanceId(notesheet.getApplicationInstanceId()).actionTaken(notesheet.getActionTaken())
				.documents(documentDTOs) // Set associated documents here
				.build();
	}

	@Override
	public List<NotesheetDTO> getAllNotesheets(Long applicationInstanceId) {
		List<Notesheet> notesheets;

		if (applicationInstanceId != null) {
			notesheets = notesheetRepository.findByApplicationInstanceId(applicationInstanceId);
		} else {
			notesheets = notesheetRepository.findAll();
		}

		return notesheets.stream().map(notesheet -> {
			// Fetch the user's full name by createdBy ID
			String createdByName = baseUserRepository.findById(notesheet.getCreatedBy())
					.map(user -> user.getFirstName() + " " + user.getLastName()).orElse("Unknown");

			// Fetch the role name using the createdBy user ID
			String roleName = notesheetRepository.findRoleNameByUserId(notesheet.getCreatedBy());

			// Map NoteStepwiseDocuments to ProcessStepwiseDocumentsDTO
			List<ProcessStepwiseDocumentsDTO> documents = notesheet.getDocuments().stream()
					.map(ProcessStepwiseDocumentsDTO::new).collect(Collectors.toList());

			// Fetch referenceNo using applicationInstanceId
			String referenceNo = notesheetRepository
					.findNotesheetsWithReferenceNo(notesheet.getApplicationInstanceId());

			// Map to NotesheetDTO
			return NotesheetDTO.builder().id(notesheet.getId()).note(notesheet.getNote())
					.receiverRoleId(notesheet.getReceiverRoleId()).receiverUserId(notesheet.getReceiverUserId())
					.senderRoleId(notesheet.getSenderRoleId()).applicationHistoryId(notesheet.getApplicationHistoryId())
					.applicationInstanceId(notesheet.getApplicationInstanceId()).actionTaken(notesheet.getActionTaken())
					.additionalDetails(notesheet.getAdditionalDetails()).createdByName(createdByName).roleName(roleName)
					.createdAt(notesheet.getCreatedAt()).documents(documents).referenceNo(referenceNo).build();
		}).collect(Collectors.toList());
	}

	@Override
	public NotesheetDTO updateNotesheet(Long id, NotesheetDTO notesheetDTO) {
		// Fetch the notesheet by ID, throw an exception if not found
		Notesheet notesheet = notesheetRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Notesheet not found with the given ID"));

		// Update the fields of the existing notesheet with values from the DTO
		notesheet.setNote(notesheetDTO.getNote());
		notesheet.setReceiverRoleId(notesheetDTO.getReceiverRoleId());
		notesheet.setReceiverUserId(notesheetDTO.getReceiverUserId());
		notesheet.setSenderRoleId(notesheetDTO.getSenderRoleId());
		notesheet.setApplicationHistoryId(notesheetDTO.getApplicationHistoryId()); // Correct field name
		notesheet.setApplicationInstanceId(notesheetDTO.getApplicationInstanceId());
		notesheet.setActionTaken(notesheetDTO.getActionTaken());

		// Save the updated entity back to the repository
		notesheet = notesheetRepository.save(notesheet);

		// Return the updated entity as a DTO
		return NotesheetDTO.builder().note(notesheet.getNote()).receiverRoleId(notesheet.getReceiverRoleId())
				.receiverUserId(notesheet.getReceiverUserId()).senderRoleId(notesheet.getSenderRoleId())
				.applicationHistoryId(notesheet.getApplicationHistoryId()) // Correct field name
				.applicationInstanceId(notesheet.getApplicationInstanceId()).actionTaken(notesheet.getActionTaken())
				// .additionalDetails(notesheet.getAdditionalDetails()) // Include additional
				// details if needed
				.build();
	}

	@Override
	public void deleteNotesheet(Long id) {
		notesheetRepository.deleteById(id);
	}

}
