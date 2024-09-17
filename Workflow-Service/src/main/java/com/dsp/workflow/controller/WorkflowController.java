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
import com.dsp.workflow.dto.TaskIdRequestDTO;
import com.dsp.workflow.dto.TaskResponseDTO;
import com.dsp.workflow.response.GenericResponse;
import com.dsp.workflow.service.WorkflowService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/workflow")
public class WorkflowController {

	@Autowired
	private WorkflowService workflowService;

	@PostMapping("/start/{workflowId}")
	public GenericResponse<Map<String, String>> startWorkflow(@PathVariable("workflowId") String workflowId) {

		// Start the process instance with the given workflowId
		String processInstanceId = workflowService.startProcessInstance(workflowId);

		// Retrieve the task ID associated with the process instance
		String taskId = workflowService.getTaskIdByProcessInstanceId(processInstanceId);

		// Prepare response data
		Map<String, String> responseData = new HashMap<>();
		responseData.put("processInstanceId", processInstanceId);
		responseData.put("taskId", taskId);

		// Return success response with process instance ID and task ID
		return new GenericResponse<>(true, "Workflow started successfully", responseData, HttpStatus.OK.value());
	}

	@PostMapping("/completeTask")
	public GenericResponse<String> completeTask(@Valid @RequestBody CompleteTaskRequestDTO completeTaskRequestDTO) {

		String taskId = completeTaskRequestDTO.getTaskId();
		// Create a map of variables to be passed
		Map<String, Object> variables = new HashMap<>();
		variables.put("note", completeTaskRequestDTO.getNote());
		variables.put("receiverRoleId", completeTaskRequestDTO.getReceiverRoleId());
		variables.put("receiverUserId", completeTaskRequestDTO.getReceiverUserId());
		variables.put("senderRoleId", completeTaskRequestDTO.getSenderRoleId());
		variables.put("historyId", taskId);
		variables.put("applicationInstanceId", completeTaskRequestDTO.getApplicationInstanceId());
		variables.put("actionTaken", completeTaskRequestDTO.getActionTaken());

		variables.put("sendTo",
				completeTaskRequestDTO.getSendTo() != null ? completeTaskRequestDTO.getSendTo().name() : null);

		// Complete the task using the workflow service
		String newTaskId = workflowService.completeTask(taskId, variables);

		if (newTaskId != null) {
			return new GenericResponse<>(true, "Task completed successfully", newTaskId, HttpStatus.OK.value());
		} else {
			return new GenericResponse<>(false, "Failed to complete task", taskId, HttpStatus.BAD_REQUEST.value());
		}
	}

	@GetMapping("/pending")
	public ResponseEntity<List<TaskResponseDTO>> getPendingTasks(
			@RequestParam("receiverUserId") Integer receiverUserId) {
		List<TaskResponseDTO> pendingTasks = workflowService.getPendingTasksByReceiverUserId(receiverUserId);
		return ResponseEntity.ok(pendingTasks);
	}

	@PostMapping("/historic-tasks")
	public ResponseEntity<List<HistoricTaskInstance>> getHistoricTasks(@RequestBody TaskIdRequestDTO requestDTO) {
		List<HistoricTaskInstance> tasks = workflowService.getHistoricTasksByTaskId(requestDTO.getTaskId());
		return new ResponseEntity<>(tasks, HttpStatus.OK);
	}
}
