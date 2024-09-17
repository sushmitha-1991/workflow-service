/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import jakarta.inject.Named;

/**
 * 
 */
@Named
public class ReviewTheDocument implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		String sendToReviewer = "";
		String commentsByReviewer = "";
		sendToReviewer = (String) execution.getVariable("sendToReviewer");

		if ("Reviewer".equals(sendToReviewer)) {
			commentsByReviewer = "Document is in Reviewing State";
		} else if ("Publish".equals(sendToReviewer)) {
			commentsByReviewer = "Please publish it";

		}
		execution.setVariable("commentsByReviewer", commentsByReviewer);

	}

}
