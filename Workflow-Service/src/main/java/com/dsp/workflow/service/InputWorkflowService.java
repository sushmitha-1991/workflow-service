package com.dsp.workflow.service;

import java.util.List;

import com.dsp.workflow.dto.DocumentMetadataDTO;
import com.dsp.workflow.entity.WorkflowHistory;

public interface InputWorkflowService {

	/**
	 * 
	 * @param inputInstanceId
	 * @return
	 */
	List<WorkflowHistory> getInputHistoryByInputInstanceId(Long inputInstanceId); 

	List<DocumentMetadataDTO> getDocuments(Long inputInstanceId);

}
