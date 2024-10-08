/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dsp.workflow.dto.CompleteTaskRequestDTO;
import com.dsp.workflow.dto.PublishRequestDTO;
import com.dsp.workflow.dto.TaskHistoryDTO;
import com.dsp.workflow.dto.TaskIdRequestDTO;
import com.dsp.workflow.dto.TaskResponseDTO;
import com.dsp.workflow.notesheet.dto.NotesheetDTO;
import com.dsp.workflow.response.GenericResponse;
import com.dsp.workflow.service.WorkflowService;
import com.dsp.workflow.util.Utils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/workflow")
public class WorkflowController {

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private Utils utils;

	/** Start the Workflow Engine **/
	@PostMapping("/start/{workflowId}")
	public GenericResponse<Map<String, String>> startWorkflow(@PathVariable("workflowId") String workflowId,
			@RequestBody NotesheetDTO notesheetDTO) {
		Long senderRoleId = utils.getRoleIdByUserId();
		String processInstanceId = workflowService.startProcessInstance(workflowId);
		String taskId = workflowService.getTaskIdByProcessInstanceId(processInstanceId);

		// Save the notesheet and application instance details
		workflowService.insertApplicationInstance(notesheetDTO, processInstanceId, taskId, senderRoleId);

		// Prepare response
		Map<String, String> responseData = new HashMap<>();
		responseData.put("processInstanceId", processInstanceId);
		responseData.put("taskId", taskId);

		return new GenericResponse<>(true, "Workflow started successfully", responseData, HttpStatus.OK.value());
	}

	/** Complete the assigned tasks **/
	@PostMapping("/completeTask")
	public GenericResponse<String> completeTask(@Valid @RequestBody CompleteTaskRequestDTO completeTaskRequestDTO) {

		String taskId = completeTaskRequestDTO.getTaskId();

		// Create a map of variables to be passed
		Map<String, Object> variables = new HashMap<>();
		variables.put("processInstanceId", completeTaskRequestDTO.getProcessInstanceId());
		variables.put("note", completeTaskRequestDTO.getNote());
		variables.put("receiverRoleId", completeTaskRequestDTO.getReceiverRoleId());
		variables.put("receiverUserId", completeTaskRequestDTO.getReceiverUserId());

		// Use the Utils method to fetch the senderRoleId
		Long senderRoleId = utils.getRoleIdByUserId();
		variables.put("senderRoleId", senderRoleId); // Set senderRoleId here

		variables.put("historyId", taskId);
		variables.put("applicationInstanceId", completeTaskRequestDTO.getApplicationInstanceId());
		variables.put("actionTaken", completeTaskRequestDTO.getActionTaken());
		variables.put("sendTo",
				completeTaskRequestDTO.getSendTo() != null ? completeTaskRequestDTO.getSendTo().name() : null);
		variables.put("officeId", completeTaskRequestDTO.getOfficeId());
		variables.put("application_no", completeTaskRequestDTO.getApplication_no());
		variables.put("additional_details", completeTaskRequestDTO.getAdditional_details());

		// Complete the task using the workflow service
		String newTaskId = workflowService.completeTask(taskId, variables);

		if (newTaskId != null) {
			return new GenericResponse<>(true, "Task completed successfully", newTaskId, HttpStatus.OK.value());
		} else {
			return new GenericResponse<>(false, "Failed to complete task", taskId, HttpStatus.BAD_REQUEST.value());
		}
	}

	/*Get Pending task list using Receiver User ID*/
	@GetMapping("/pending")
	public ResponseEntity<List<TaskResponseDTO>> getPendingTasks(
			@RequestParam("receiverUserId") Integer receiverUserId) {
		List<TaskResponseDTO> pendingTasks = workflowService.getTasksByReceiverUserId(receiverUserId);
		return ResponseEntity.ok(pendingTasks);
	}

	@GetMapping("/process-instance/{processInstanceId}")
	public GenericResponse<List<TaskHistoryDTO>> getTasksByProcessInstanceId(
			@PathVariable("processInstanceId") String processInstanceId) {
		List<TaskHistoryDTO> taskHistoryList = workflowService.getTasksByProcessInstanceId(processInstanceId);

		if (!taskHistoryList.isEmpty()) {
			return new GenericResponse<>(true, "Tasks fetched successfully", taskHistoryList, HttpStatus.OK.value());
		} else {
			return new GenericResponse<>(false, "No tasks found for the given process instance", null,
					HttpStatus.NOT_FOUND.value());
		}
	}

	@PostMapping("/publish")
	public ResponseEntity<GenericResponse<String>> publishTask(@Valid @RequestBody PublishRequestDTO requestDTO) {
		try {
			boolean isApproved = workflowService.isDocumentApprovedForPublishing(requestDTO.getTaskId());
			if (!isApproved) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GenericResponse<>(false,
						"Document is not approved for publishing", null, HttpStatus.BAD_REQUEST.value()));
			}
			String result = workflowService.publishDocument(requestDTO);
			return ResponseEntity
					.ok(new GenericResponse<>(true, "Document published successfully", result, HttpStatus.OK.value()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericResponse<>(false,
					"An error occurred while publishing the document", null, HttpStatus.INTERNAL_SERVER_ERROR.value()));
		}
	}

}
