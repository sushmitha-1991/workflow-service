package com.dsp.workflow.service.impl;


import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

}
