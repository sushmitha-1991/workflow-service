package com.dsp.workflow.notesheet.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dsp.workflow.notesheet.dto.NotesheetDTO;
import com.dsp.workflow.notesheet.entity.Notesheet;
import com.dsp.workflow.notesheet.repository.NotesheetRepository;
import com.dsp.workflow.notesheet.service.NotesheetService;

@Service
public class NotesheetServiceImpl implements NotesheetService {

	@Autowired
	private NotesheetRepository notesheetRepository;

	@Override
	public NotesheetDTO createNotesheet(NotesheetDTO notesheetDTO) {
		// Build the Notesheet entity from the DTO
		Notesheet notesheet = Notesheet.builder().note(notesheetDTO.getNote())
				.receiverRoleId(notesheetDTO.getReceiverRoleId()).receiverUserId(notesheetDTO.getReceiverUserId())
				.senderRoleId(notesheetDTO.getSenderRoleId())
				.applicationHistoryId(notesheetDTO.getApplicationhistoryId()) // Use the correct name
				.applicationInstanceId(notesheetDTO.getApplicationInstanceId())
				.actionTaken(notesheetDTO.getActionTaken())
				// .additionalDetails(notesheetDTO.getAdditionalDetails()) // Map additional
				// details
				.build();

		// Save the entity to the repository
		notesheet = notesheetRepository.save(notesheet);

		// Return the saved entity mapped back to the DTO
		return NotesheetDTO.builder().note(notesheet.getNote()).receiverRoleId(notesheet.getReceiverRoleId())
				.receiverUserId(notesheet.getReceiverUserId()).senderRoleId(notesheet.getSenderRoleId())
				.applicationhistoryId(notesheet.getApplicationHistoryId()) // Correct name
				.applicationInstanceId(notesheet.getApplicationInstanceId()).actionTaken(notesheet.getActionTaken())
				// .additionalDetails(notesheet.getAdditionalDetails()) // Map additional
				// details back to the DTO
				.build();
	}

	@Override
	public NotesheetDTO getNotesheetById(Long id) {
		// Fetch the notesheet by ID, throw an exception if not found
		Notesheet notesheet = notesheetRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Notesheet not found with the given ID"));

		// Return a NotesheetDTO built from the Notesheet entity
		return NotesheetDTO.builder().note(notesheet.getNote()).receiverRoleId(notesheet.getReceiverRoleId())
				.receiverUserId(notesheet.getReceiverUserId()).senderRoleId(notesheet.getSenderRoleId())
				.applicationhistoryId(notesheet.getApplicationHistoryId()) // Use the correct field name
				.applicationInstanceId(notesheet.getApplicationInstanceId()).actionTaken(notesheet.getActionTaken())
				// .additionalDetails(notesheet.getAdditionalDetails()) // Include additional
				// details if needed
				.build();
	}

	@Override
	public List<NotesheetDTO> getAllNotesheets() {
		// Fetch all notesheets, map each to NotesheetDTO
		return notesheetRepository.findAll().stream().map(notesheet -> NotesheetDTO.builder().note(notesheet.getNote())
				.receiverRoleId(notesheet.getReceiverRoleId()).receiverUserId(notesheet.getReceiverUserId())
				.senderRoleId(notesheet.getSenderRoleId()).applicationhistoryId(notesheet.getApplicationHistoryId()) // Correct
																														// field
																														// name
				.applicationInstanceId(notesheet.getApplicationInstanceId()).actionTaken(notesheet.getActionTaken())
				// .additionalDetails(notesheet.getAdditionalDetails()) // Include additional
				// details if needed
				.build()).collect(Collectors.toList());
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
		notesheet.setApplicationHistoryId(notesheetDTO.getApplicationhistoryId()); // Correct field name
		notesheet.setApplicationInstanceId(notesheetDTO.getApplicationInstanceId());
		notesheet.setActionTaken(notesheetDTO.getActionTaken());

		// Save the updated entity back to the repository
		notesheet = notesheetRepository.save(notesheet);

		// Return the updated entity as a DTO
		return NotesheetDTO.builder().note(notesheet.getNote()).receiverRoleId(notesheet.getReceiverRoleId())
				.receiverUserId(notesheet.getReceiverUserId()).senderRoleId(notesheet.getSenderRoleId())
				.applicationhistoryId(notesheet.getApplicationHistoryId()) // Correct field name
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
