package com.dsp.workflow.service.impl;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dsp.workflow.entity.InputWorkflowHistory;
import com.dsp.workflow.repository.InputWorkflowRepository;
import com.dsp.workflow.service.InputWorkflowService;

import jakarta.servlet.http.HttpServletRequest;



@Service
public class InputWorkflowServiceImpl implements InputWorkflowService {

	@Autowired
	private InputWorkflowRepository inputHistoryRepository;

	@Override
	public List<InputWorkflowHistory> getInputHistoryByInputInstanceId(Long inputInstanceId) {
		return inputHistoryRepository.findByInputInstanceId(inputInstanceId); 
	}

}
