package com.dsp.workflow.notesheet.service;

import com.dsp.workflow.notesheet.dto.NotesheetDTO;

import java.util.List;

public interface NotesheetService {

    NotesheetDTO createNotesheet(NotesheetDTO notesheetDTO);

    NotesheetDTO getNotesheetById(Long id);

    List<NotesheetDTO> getAllNotesheets();

    NotesheetDTO updateNotesheet(Long id, NotesheetDTO notesheetDTO);

    void deleteNotesheet(Long id);

    List<NotesheetDTO> getNotesheetHistory(int historyId);
}
