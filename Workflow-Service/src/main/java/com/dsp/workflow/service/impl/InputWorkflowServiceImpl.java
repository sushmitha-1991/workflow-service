package com.dsp.workflow.service.impl;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dsp.workflow.dto.DocumentMetadataDTO;
import com.dsp.workflow.entity.InputWorkflowHistory;
import com.dsp.workflow.entity.WorkflowHistory;
import com.dsp.workflow.mapper.WorkflowHistoryMapper;
import com.dsp.workflow.repository.InputWorkflowRepository;
import com.dsp.workflow.service.InputWorkflowService;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class InputWorkflowServiceImpl implements InputWorkflowService {

	@Autowired
	private InputWorkflowRepository inputHistoryRepository;

	private final WorkflowHistoryMapper wfHistoryMapper;
	
	@Override
	public List<WorkflowHistory> getInputHistoryByInputInstanceId(Long inputInstanceId) {
		
		List<InputWorkflowHistory> historyList = inputHistoryRepository.findByInputInstanceId(inputInstanceId);
		
		return historyList.stream()
                .map(wfHistoryMapper::toWorkflowHistory) 
                .collect(Collectors.toList());
    }

	
	@Override
	public List<DocumentMetadataDTO> getDocuments (Long inputInstanceId) {
		
		InputWorkflowHistory history = inputHistoryRepository.findTop1ByInputInstanceIdOrderByCreatedAtDesc(inputInstanceId);
		
		if (history == null) return Collections.EMPTY_LIST;
		
		List<Map<String, Object>>docList = history.getReferenceFiles();
		
		if (docList == null) return Collections.EMPTY_LIST;
		
		return docList.stream()
                .map(wfHistoryMapper::toDocumentDTO) 
                .collect(Collectors.toList());
    
	}	
}
