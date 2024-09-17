///**
// * 
// * @author Mrinal Lakhanpal
// * @version 1.0
// */
//package com.dsp.workflow.delegate;
//
//import org.camunda.bpm.engine.delegate.DelegateExecution;
//import org.camunda.bpm.engine.delegate.JavaDelegate;
//
//import jakarta.inject.Named;
//
///**
// * 
// */
//@Named
//public class SendToPublishorCreator implements JavaDelegate {
//
//	@Override
//	public void execute(DelegateExecution execution) throws Exception {
//		// TODO Auto-generated method stub
//		String reviewed = "";
//		String sendBack = "";
//		//String commentToCreator = "";
//		reviewed = (String) execution.getVariable("reviewed");
//		// returned = (String) execution.getVariable("returned");
//		sendBack = (String) execution.getVariable("sendBack");
//
//		if (reviewed == "No" && sendBack == "Creator") {
//			//commentToCreator = "Please review it back";
//		} else {
//			//commentToCreator = "Sent for Publishing";
//		}
//
//		//execution.setVariable("commentToCreator", commentToCreator);
//
//	}
//
//}
