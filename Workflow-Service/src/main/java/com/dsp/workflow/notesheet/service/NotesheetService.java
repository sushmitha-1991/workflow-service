package com.dsp.workflow.notesheet.service;

import java.util.List;

import com.dsp.workflow.notesheet.dto.NotesheetDTO;

public interface NotesheetService {

	NotesheetDTO createNotesheet(NotesheetDTO notesheetDTO, Long senderRoleId);

	NotesheetDTO getNotesheetById(Long id);

	List<NotesheetDTO> getAllNotesheets();

	NotesheetDTO updateNotesheet(Long id, NotesheetDTO notesheetDTO);

	void deleteNotesheet(Long id);

	/**
	 * @param applicationInstanceId
	 * @return
	 */
	List<NotesheetDTO> getAllNotesheets(Long applicationInstanceId);

}
