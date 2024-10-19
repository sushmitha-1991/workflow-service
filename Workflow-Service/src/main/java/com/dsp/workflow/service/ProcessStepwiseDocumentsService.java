/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.service;

import java.util.List;

import com.dsp.workflow.dto.ProcessStepwiseDocumentsDTO;

public interface ProcessStepwiseDocumentsService {

	ProcessStepwiseDocumentsDTO getDocumentById(Long id);

	List<ProcessStepwiseDocumentsDTO> getAllDocuments();

	ProcessStepwiseDocumentsDTO createDocument(ProcessStepwiseDocumentsDTO documentDTO);

	ProcessStepwiseDocumentsDTO updateDocument(Long id, ProcessStepwiseDocumentsDTO documentDTO);

	void deleteDocument(Long id);
}
