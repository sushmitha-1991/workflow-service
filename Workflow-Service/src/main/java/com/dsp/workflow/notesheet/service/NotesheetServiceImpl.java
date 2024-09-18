package com.dsp.workflow.notesheet.service;


import com.dsp.workflow.notesheet.dto.NotesheetDTO;
import com.dsp.workflow.notesheet.entity.Notesheet;
import com.dsp.workflow.notesheet.repository.NotesheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotesheetServiceImpl implements NotesheetService{

    @Autowired
   private NotesheetRepository notesheetRepository;

    @Override
    public NotesheetDTO createNotesheet(NotesheetDTO notesheetDTO) {
        Notesheet notesheet = Notesheet.builder()
                .note(notesheetDTO.getNote())
                .receiverRoleId(notesheetDTO.getReceiverRoleId())
                .receiverUserId(notesheetDTO.getReceiverUserId())
                .senderRoleId(notesheetDTO.getSenderRoleId())
                .historyId(notesheetDTO.getHistoryId())
                .applicationInstanceId(notesheetDTO.getApplicationInstanceId())
                .actionTaken(notesheetDTO.getActionTaken())
                .build();

        notesheet = notesheetRepository.save(notesheet);

        return NotesheetDTO.builder()
                .note(notesheet.getNote())
                .receiverRoleId(notesheet.getReceiverRoleId())
                .receiverUserId(notesheet.getReceiverUserId())
                .senderRoleId(notesheet.getSenderRoleId())
                .historyId(notesheet.getHistoryId())
                .applicationInstanceId(notesheet.getApplicationInstanceId())
                .actionTaken(notesheet.getActionTaken())
                .build();
    }

    @Override
    public NotesheetDTO getNotesheetById(Long id) {
        Notesheet notesheet = notesheetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notesheet not found with given ID"));

        return NotesheetDTO.builder()
                .note(notesheet.getNote())
                .receiverRoleId(notesheet.getReceiverRoleId())
                .receiverUserId(notesheet.getReceiverUserId())
                .senderRoleId(notesheet.getSenderRoleId())
                .historyId(notesheet.getHistoryId())
                .applicationInstanceId(notesheet.getApplicationInstanceId())
                .actionTaken(notesheet.getActionTaken())
                .build();
    }

    @Override
    public List<NotesheetDTO> getAllNotesheets() {
        return notesheetRepository.findAll().stream()
                .map(notesheet -> NotesheetDTO.builder()
                        .note(notesheet.getNote())
                        .receiverRoleId(notesheet.getReceiverRoleId())
                        .receiverUserId(notesheet.getReceiverUserId())
                        .senderRoleId(notesheet.getSenderRoleId())
                        .historyId(notesheet.getHistoryId())
                        .applicationInstanceId(notesheet.getApplicationInstanceId())
                        .actionTaken(notesheet.getActionTaken())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public NotesheetDTO updateNotesheet(Long id, NotesheetDTO notesheetDTO) {
        Notesheet notesheet = notesheetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notesheet not found with given ID"));

        notesheet.setNote(notesheetDTO.getNote());
        notesheet.setReceiverRoleId(notesheetDTO.getReceiverRoleId());
        notesheet.setReceiverUserId(notesheetDTO.getReceiverUserId());
        notesheet.setSenderRoleId(notesheetDTO.getSenderRoleId());
        notesheet.setHistoryId(notesheetDTO.getHistoryId());
        notesheet.setApplicationInstanceId(notesheetDTO.getApplicationInstanceId());
        notesheet.setActionTaken(notesheetDTO.getActionTaken());

        notesheet = notesheetRepository.save(notesheet);

        return NotesheetDTO.builder()
                .note(notesheet.getNote())
                .receiverRoleId(notesheet.getReceiverRoleId())
                .receiverUserId(notesheet.getReceiverUserId())
                .senderRoleId(notesheet.getSenderRoleId())
                .historyId(notesheet.getHistoryId())
                .applicationInstanceId(notesheet.getApplicationInstanceId())
                .actionTaken(notesheet.getActionTaken())
                .build();
    }

    @Override
    public void deleteNotesheet(Long id) {
        notesheetRepository.deleteById(id);
    }

    @Override
    public List<NotesheetDTO> getNotesheetHistory(int historyId) {
        List<Notesheet> history = notesheetRepository.findByHistoryId(historyId);
        return history.stream()
                .map(NotesheetDTO::new) // This will now work
                .collect(Collectors.toList());
    }

}
