/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.dsp.workflow.dto.TaskHistoryDTO;
import com.dsp.workflow.dto.TaskResponseDTO;
import com.dsp.workflow.entity.WorkflowHistory;
import com.dsp.workflow.notesheet.dto.NotesheetDTO;
import com.dsp.workflow.response.GenericResponse;
import com.dsp.workflow.service.WorkflowService;
import com.dsp.workflow.util.Utils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/workflow")
@Tag(name = "Workflow APIs", description = "APIs related to Camunda Workflow Engine management")
public class WorkflowController {

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private Utils utils;

	@Operation(summary = "Start Workflow Engine", description = "Starts a workflow instance with the provided workflow ID and notesheet details.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Workflow started successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data"),
			@ApiResponse(responseCode = "500", description = "Failed to start workflow due to internal error") })
	@PostMapping("/start/{workflowId}")
	public GenericResponse<Map<String, String>> startWorkflow(@PathVariable("workflowId") String workflowId,
			@RequestBody NotesheetDTO notesheetDTO) {
		Long senderRoleId = utils.getRoleIdByUserId();
		String processInstanceId = workflowService.startProcessInstance(workflowId);
		String taskId = workflowService.getTaskIdByProcessInstanceId(processInstanceId);

		workflowService.updateApplicationInstance(notesheetDTO, processInstanceId, taskId);

		Map<String, String> responseData = new HashMap<>();
		responseData.put("processInstanceId", processInstanceId);
		responseData.put("taskId", taskId);

		return new GenericResponse<>(true, "Workflow started successfully", responseData, HttpStatus.OK.value());
	}

	@Operation(summary = "Complete Assigned Task", description = "Completes a specific task in the workflow with the provided task details.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Task completed successfully"),
			@ApiResponse(responseCode = "400", description = "Failed to complete task due to invalid data"),
			@ApiResponse(responseCode = "500", description = "Error completing task") })
	@PostMapping("/completeTask")
	public GenericResponse<String> completeTask(@Valid @RequestBody CompleteTaskRequestDTO completeTaskRequestDTO) {
		String taskId = completeTaskRequestDTO.getTaskId();
		Map<String, Object> variables = new HashMap<>();
		variables.put("processInstanceId", completeTaskRequestDTO.getProcessInstanceId());
		variables.put("note", completeTaskRequestDTO.getNote());
		variables.put("receiverRoleId", completeTaskRequestDTO.getReceiverRoleId());
		variables.put("receiverUserId", completeTaskRequestDTO.getReceiverUserId());
		variables.put("receiverUserName", completeTaskRequestDTO.getReceiverUserName());
		variables.put("senderRoleId", utils.getRoleIdByUserId());
		variables.put("senderUserId", utils.getUserIdFromAuthentication());
		variables.put("historyId", taskId);
		variables.put("applicationInstanceId", completeTaskRequestDTO.getApplicationInstanceId());
		variables.put("actionTaken", completeTaskRequestDTO.getActionTaken());
		variables.put("sendTo",
				completeTaskRequestDTO.getSendTo() != null ? completeTaskRequestDTO.getSendTo().name() : null);
		variables.put("officeId", completeTaskRequestDTO.getOfficeId());
		variables.put("application_no", completeTaskRequestDTO.getApplication_no());
		variables.put("additional_details", completeTaskRequestDTO.getAdditional_details());
		variables.put("stepType", completeTaskRequestDTO.getStepType());
		variables.put("status", completeTaskRequestDTO.getStatus());
		// variables.put("document", completeTaskRequestDTO.getDocument());

		String newTaskId = workflowService.completeTask(taskId, variables, completeTaskRequestDTO.getDocument());

		if (newTaskId != null) {
			return new GenericResponse<>(true, "Task completed successfully", newTaskId, HttpStatus.OK.value());
		} else {
			return new GenericResponse<>(false, "Failed to complete task", taskId, HttpStatus.BAD_REQUEST.value());
		}
	}

	@Operation(summary = "Get Pending Tasks", description = "Retrieves a list of pending tasks for a given receiver user ID.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Pending tasks retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "No pending tasks found") })
	@GetMapping("/pending")
	public ResponseEntity<GenericResponse<List<TaskResponseDTO>>> getPendingTasks() {
		Long receiverUserId = utils.getUserIdFromAuthentication();
		System.out.println("### receiverUserId - " + receiverUserId);

		List<TaskResponseDTO> pendingTasks = workflowService.getTasksByReceiverUserId(receiverUserId);

		// Constructing the response using GenericResponse
		GenericResponse<List<TaskResponseDTO>> response = new GenericResponse<>(true,
				"Pending tasks retrieved successfully", pendingTasks, HttpStatus.OK.value());

		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Get Task History by Process Instance ID", description = "Fetches all task history associated with a given process instance ID.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Tasks fetched successfully"),
			@ApiResponse(responseCode = "404", description = "No tasks found for the given process instance") })
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

	@GetMapping("/bySubmodule")
	public ResponseEntity<GenericResponse<WorkflowHistory>> getWorkflowHistory(@RequestParam String subModuleId) {
		Long userId = utils.getUserIdFromAuthentication();
		WorkflowHistory history = workflowService.getMostRecentWorkflowHistoryBySubModuleIdAndActionTakenBy(subModuleId,
				userId);

		if (history == null) {
			GenericResponse<WorkflowHistory> response = new GenericResponse<>(false, "No records found", null, 200);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}

		GenericResponse<WorkflowHistory> response = new GenericResponse<>(true, "Record found", history, 200);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/historyByApplicationInstance")
	public ResponseEntity<GenericResponse<List<WorkflowHistory>>> getWorkflowHistoryByApplicationInstanceId(
			@RequestParam Long applicationInstanceId) {
		List<WorkflowHistory> histories = workflowService
				.getWorkflowHistoryByApplicationInstanceId(applicationInstanceId);
		if (histories.isEmpty()) {
			GenericResponse<List<WorkflowHistory>> response = new GenericResponse<>(false, "No records found",
					histories, 200);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}
		GenericResponse<List<WorkflowHistory>> response = new GenericResponse<>(true, "Records found", histories, 200);
		return ResponseEntity.ok(response);
	}

}
