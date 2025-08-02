/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.mapper;

import org.springframework.stereotype.Component;

import com.dsp.workflow.entity.InputWorkflowHistory;
import com.dsp.workflow.entity.WorkflowHistory;

@Component
public class WorkflowHistoryMapper {


	public WorkflowHistory toWorkflowHistory (InputWorkflowHistory input) {

		WorkflowHistory history = WorkflowHistory.builder()
									.actionTakenBy(input.getActionTakenBy())
									.actionTakenByName(input.getActionTakenByName())
									.actionTakenOn(input.getActionTakenOn().toLocalDateTime())
									.status(input.getStatus())
									.pendingWith(input.getPendingWith())
									.pendingWithName(input.getPendingWithName())
									.note(input.getNote())
									.build();
		return history;
	}

}
