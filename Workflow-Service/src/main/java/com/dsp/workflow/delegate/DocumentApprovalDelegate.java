package com.dsp.workflow.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("documentApprovalDelegate") // The name to use in the BPMN diagram's delegate expression
public class DocumentApprovalDelegate implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		// Update the flag that the document is approved and ready for publishing
		Boolean isApprovedForPublishing = true;

		// Set the flag as a process variable
		execution.setVariable("isApprovedForPublishing", isApprovedForPublishing);

		// You can also log this information for tracking purposes
		System.out.println("Document marked as approved and ready for publishing. Process Instance ID: "
				+ execution.getProcessInstanceId());

		// The flag is now available to the next user task (Publish)
	}
}
