package com.dsp.workflow.service;

import java.util.List;

import com.dsp.workflow.entity.InputWorkflowHistory;

public interface InputWorkflowService {

	/**
	 * 
	 * @param inputInstanceId
	 * @return
	 */
	List<InputWorkflowHistory> getInputHistoryByInputInstanceId(Long inputInstanceId); 

}
