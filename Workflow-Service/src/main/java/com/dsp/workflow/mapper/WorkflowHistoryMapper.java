/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.mapper;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.dsp.workflow.dto.DocumentMetadataDTO;
import com.dsp.workflow.entity.InputWorkflowHistory;
import com.dsp.workflow.entity.WorkflowHistory;

@Component
public class WorkflowHistoryMapper {


	public WorkflowHistory toWorkflowHistory (InputWorkflowHistory input) {

		WorkflowHistory history = WorkflowHistory.builder()
									.actionTakenBy(input.getActionTakenBy())
									.actionTakenByName(input.getActionTakenByName())
									.actionTakenOn(input.getCreatedAt())
									.status(input.getStatus())
									.pendingWith(input.getPendingWith())
									.pendingWithName(input.getPendingWithName())
									.note(input.getCommentForDisplay())
									.build();
		return history;
	}

	/**
	 * 
	 * @param docMap
	 * @return
	 */
	public DocumentMetadataDTO toDocumentDTO (Map<String,Object> docMap) {

		DocumentMetadataDTO document = DocumentMetadataDTO.builder()
				.documentId(docMap.get("documentName") != null ? docMap.get("documentName").toString() : "")
				.uuid(UUID.fromString(docMap.get("documentUuid").toString())) 
				.build();
				
		return document;
	}
	
	
}
