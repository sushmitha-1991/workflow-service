/**
 *
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dsp.workflow.dto.CompleteTaskRequestDTO;
import com.dsp.workflow.dto.DraftDataDTO;
import com.dsp.workflow.dto.InProgressCountResponse;
import com.dsp.workflow.dto.PublishHistoryDTO;
import com.dsp.workflow.dto.TaskHistoryDTO;
import com.dsp.workflow.dto.TaskQueryDTO;
import com.dsp.workflow.dto.TaskResponseDTO;
import com.dsp.workflow.entity.InputWorkflowHistory;
import com.dsp.workflow.entity.WorkflowHistory;
import com.dsp.workflow.notesheet.dto.NotesheetDTO;
import com.dsp.workflow.response.GenericResponse;
import com.dsp.workflow.service.InputWorkflowService;
import com.dsp.workflow.service.PublishService;
import com.dsp.workflow.service.WorkflowService;
import com.dsp.workflow.util.Utils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping()
@Tag(name = "Workflow APIs", description = "APIs related to Camunda Workflow Engine management")
public class WorkflowController {

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private InputWorkflowService inputWorkflowService;

	@Autowired
	private PublishService publishService;
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

	// @Operation(summary = "Complete Assigned Task", description = "Completes a
	// specific task in the workflow with the provided task details.")
	// @ApiResponses({ @ApiResponse(responseCode = "200", description = "Task
	// completed successfully"),
	// @ApiResponse(responseCode = "400", description = "Failed to complete task due
	// to invalid data"),
	// @ApiResponse(responseCode = "500", description = "Error completing task") })
	// @PostMapping("/completeTask")
	// public GenericResponse<String> completeTask(@Valid @RequestBody
	// CompleteTaskRequestDTO completeTaskRequestDTO) {
	// String taskId = completeTaskRequestDTO.getTaskId();
	// Map<String, Object> variables = new HashMap<>();
	// variables.put("processInstanceId",
	// completeTaskRequestDTO.getProcessInstanceId());
	// variables.put("note", completeTaskRequestDTO.getNote());
	// variables.put("receiverRoleId", completeTaskRequestDTO.getReceiverRoleId());
	// variables.put("receiverUserId", completeTaskRequestDTO.getReceiverUserId());
	// variables.put("receiverUserName",
	// completeTaskRequestDTO.getReceiverUserName());
	// variables.put("senderRoleId", utils.getRoleIdByUserId());
	// variables.put("senderUserId", utils.getUserIdFromAuthentication());
	// variables.put("historyId", taskId);
	// variables.put("applicationInstanceId",
	// completeTaskRequestDTO.getApplicationInstanceId());
	// variables.put("actionTaken", completeTaskRequestDTO.getActionTaken());
	// variables.put("sendTo",
	// completeTaskRequestDTO.getSendTo() != null ?
	// completeTaskRequestDTO.getSendTo().name() : null);
	// variables.put("officeId", completeTaskRequestDTO.getOfficeId());
	// variables.put("application_no", completeTaskRequestDTO.getApplication_no());
	// variables.put("additional_details",
	// completeTaskRequestDTO.getAdditional_details());
	// variables.put("stepType", completeTaskRequestDTO.getStepType());
	// variables.put("status", completeTaskRequestDTO.getStatus());
	// // variables.put("document", completeTaskRequestDTO.getDocument());
	//
	// String newTaskId = workflowService.completeTask(taskId, variables,
	// completeTaskRequestDTO.getDocument(),
	// completeTaskRequestDTO.getActionUrl());
	//
	// if (newTaskId != null) {
	// return new GenericResponse<>(true, "Task completed successfully", newTaskId,
	// HttpStatus.OK.value());
	// } else {
	// return new GenericResponse<>(false, "Failed to complete task", taskId,
	// HttpStatus.BAD_REQUEST.value());
	// }
	// }
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

	// API for All documents
	@GetMapping("/allDocumentsTab")
	public ResponseEntity<GenericResponse<List<Map<String, Object>>>> getallDocumentsTab() {
		Long userId = utils.getUserIdFromAuthentication();
		List<Map<String, Object>> data = workflowService.getallDocumentsTab(userId);
		if (data.isEmpty()) {
			return ResponseEntity.ok(new GenericResponse<>(false, "No records found", data, 200));
		}
		return ResponseEntity.ok(new GenericResponse<>(true, "Records found", data, 200));
	}

	// API for All documents
	@GetMapping("/allApprovedTab")
	public ResponseEntity<GenericResponse<List<Map<String, Object>>>> getallApprovedTab() {
		Long userId = utils.getUserIdFromAuthentication();
		List<Map<String, Object>> data = workflowService.getallApprovedTab(userId);
		if (data.isEmpty()) {
			return ResponseEntity.ok(new GenericResponse<>(false, "No records found", data, 200));
		}
		return ResponseEntity.ok(new GenericResponse<>(true, "Records found", data, 200));
	}

	@Operation(summary = "Start or Complete Task", description = "Handles the initiation or completion of a task based on the stepType.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Task processed successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data"),
			@ApiResponse(responseCode = "500", description = "Internal error processing task") })
	@PostMapping("/processTask")
	public GenericResponse<String> processTask(@Valid @RequestBody CompleteTaskRequestDTO completeTaskRequestDTO) {
		String processInstanceId;
		String taskId;

		if ("INIT".equals(completeTaskRequestDTO.getStepType())) {
			// Start workflow and fetch processInstanceId and taskId
			processInstanceId = workflowService.startProcessInstance(completeTaskRequestDTO.getWorkflowId());
			taskId = workflowService.getTaskIdByProcessInstanceId(processInstanceId);

			// Update application instance after starting the workflow
			workflowService.updateApplicationInstance(completeTaskRequestDTO.getApplicationInstanceId(),
					processInstanceId, taskId, completeTaskRequestDTO.getStatus());

		} else if ("INTER".equals(completeTaskRequestDTO.getStepType())
				|| "FINAL".equals(completeTaskRequestDTO.getStepType())) {
			processInstanceId = completeTaskRequestDTO.getProcessInstanceId();
			taskId = completeTaskRequestDTO.getTaskId();
		} else {
			return new GenericResponse<>(false, "Invalid stepType provided", null, HttpStatus.BAD_REQUEST.value());
		}

		// Proceed with completing the task regardless of the stepType
		Map<String, Object> variables = new HashMap<>();
		variables.put("processInstanceId", processInstanceId);
		// variables.put("note", completeTaskRequestDTO.getNote());
		variables.put("receiverRoleId",
				completeTaskRequestDTO.getReceiverRoleId() != null ? completeTaskRequestDTO.getReceiverRoleId() : 0);
		variables.put("receiverUserId",
				completeTaskRequestDTO.getReceiverUserId() != null ? completeTaskRequestDTO.getReceiverUserId() : 0);
		variables.put("receiverUserName",
				completeTaskRequestDTO.getReceiverUserName() != null ? completeTaskRequestDTO.getReceiverUserName()
						: "");
		// variables.put("senderRoleId", utils.getRoleIdByUserId());
		variables.put("senderUserId", utils.getUserIdFromAuthentication());
		variables.put("historyId", taskId);
		variables.put("applicationInstanceId", completeTaskRequestDTO.getApplicationInstanceId());
		variables.put("actionTaken", completeTaskRequestDTO.getActionTaken());
		variables.put("sendTo",
				completeTaskRequestDTO.getSendTo() != null ? completeTaskRequestDTO.getSendTo().name() : null);
		variables.put("officeId",
				completeTaskRequestDTO.getOfficeId() != null ? completeTaskRequestDTO.getOfficeId() : 0);
		variables.put("application_no", completeTaskRequestDTO.getApplication_no());
		variables.put("additional_details", completeTaskRequestDTO.getAdditional_details());
		variables.put("stepType", completeTaskRequestDTO.getStepType());
		variables.put("status", completeTaskRequestDTO.getStatus());

		// Complete the task with provided variables and document list
		String newTaskId = workflowService.completeTask(taskId, variables, completeTaskRequestDTO.getDocument(),
				completeTaskRequestDTO.getActionUrl(), completeTaskRequestDTO.getNote());

		// Return success or failure based on the completion of the task
		if (newTaskId != null) {
			return new GenericResponse<>(true, "Task processed successfully", newTaskId, HttpStatus.OK.value());
		} else {
			return new GenericResponse<>(false, "Failed to process task", taskId, HttpStatus.BAD_REQUEST.value());
		}
	}

	// API for Filter - My Task Page
	@GetMapping("/my-task")
	public ResponseEntity<GenericResponse<List<Map<String, Object>>>> filterAllDocumentsTab(
			@RequestParam(required = false) String lsNo, @RequestParam(required = false) String lsSessionNo,
			@RequestParam(required = false) String rsSessionNo, @RequestParam(required = false) Long applicationId,
			@RequestParam(required = false) String keyword, @RequestParam(required = false) LocalDate fromDate,
			@RequestParam(required = false) LocalDate toDate) {

		Long userId = utils.getUserIdFromAuthentication();
		TaskQueryDTO queryDTO = TaskQueryDTO.builder().lsNo(lsNo).lsSessionNo(lsSessionNo).rsSessionNo(rsSessionNo)
				.applicationId(applicationId).keyword(keyword).fromDate(fromDate).toDate(toDate).build();
		List<Map<String, Object>> data = workflowService.filterAllDocumentsTab(userId, queryDTO);
		if (data.isEmpty()) {
			return ResponseEntity.ok(new GenericResponse<>(true, "No records found", data, 200));
		}
		return ResponseEntity.ok(new GenericResponse<>(true, "Records found", data, 200));
	}

	@PostMapping("/save-as-draft")
	public ResponseEntity<GenericResponse<Map<String, Object>>> saveAsDraft(@RequestBody DraftDataDTO draftDataDTO) {
		Long userId = utils.getUserIdFromAuthentication();

		// Call the service method to save the draft data
		Map<String, Object> data = workflowService.saveAsDraft(userId, draftDataDTO);

		// Return appropriate response based on whether data is found
		if (data.isEmpty()) {
			return ResponseEntity.ok(new GenericResponse<>(false, "No records found", data, 200));
		}
		return ResponseEntity.ok(new GenericResponse<>(true, "Document saved as draft", data, 200));
	}

	@GetMapping("/get-save-as-draft")
	public ResponseEntity<GenericResponse<Map<String, Object>>> getSaveAsDraft(@RequestParam("draftId") Long draftId) {
		// Call the service method to fetch the draft data
		Map<String, Object> data = workflowService.getSaveAsDraft(draftId);
		// Return appropriate response based on whether data is found
		if (data.isEmpty()) {
			return ResponseEntity.ok(new GenericResponse<>(false, "No record found", null, 200));
		}
		return ResponseEntity.ok(new GenericResponse<>(true, "Record found", data, 200));
	}

	@DeleteMapping("/delete-save-as-draft")
	public ResponseEntity<GenericResponse<String>> deleteDraftedData(
			@RequestParam("applicationNo") String applicationNo) {
		String data = workflowService.deleteDraftedData(applicationNo);
		if (StringUtils.isBlank(data)) {
			return ResponseEntity.ok(new GenericResponse<>(false, "No record found", null, 200));
		}
		return ResponseEntity.ok(new GenericResponse<>(true, "Record Deleted", data, 200));
	}

	@GetMapping("/getDraftByAppId/{applicationId}")
	public ResponseEntity<GenericResponse<List<DraftDataDTO>>> getDraftByAppId(
			@PathVariable("applicationId") Long applicationId) {
		// Call the service method to fetch the draft data
		System.out.println("######-" + applicationId);
		List<DraftDataDTO> data = workflowService.getDraftByAppId(applicationId);
		// Return appropriate response based on whether data is found
		if (data.isEmpty()) {
			return ResponseEntity.ok(new GenericResponse<>(false, "No record found", null, 200));
		}
		return ResponseEntity.ok(new GenericResponse<>(true, "Record found", data, 200));
	}

	// Adding data into publish_history
	@PostMapping("/publish/create")
	public PublishHistoryDTO savePublish(@RequestBody PublishHistoryDTO publishHistoryDTO) {
		// Call the service method to save the draft data
		PublishHistoryDTO responseDTO = publishService.save(publishHistoryDTO);
		return responseDTO;
	}

	@PostMapping("/send-to-translation")
	public ResponseEntity<GenericResponse<String>> processTranslation(
			@RequestParam("appInstId") Long applicationInstanceId) {
		try {
			boolean result = workflowService.processTranslation(applicationInstanceId);

			if (result) {
				return ResponseEntity.ok(new GenericResponse<>(true, "Translation processed successfully.", null,
						HttpStatus.OK.value()));
			} else {
				return ResponseEntity.ok(new GenericResponse<>(true, "Translation flag is false or no data found.",
						null, HttpStatus.OK.value()));
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new GenericResponse<>(false, "Translation processing failed: " + e.getMessage(), null,
							HttpStatus.INTERNAL_SERVER_ERROR.value()));
		}
	}

	/*
	 * Get the Input History
	 */
	@GetMapping("/get-input-history")
	public ResponseEntity<GenericResponse<List<InputWorkflowHistory>>> getInputHistory(
			@RequestParam Long inputInstanceId) {

		List<InputWorkflowHistory> histories = inputWorkflowService.getInputHistoryByInputInstanceId(inputInstanceId);

		if (histories.isEmpty()) {
			GenericResponse<List<InputWorkflowHistory>> response = new GenericResponse<>(false, "No records found",
					histories, HttpStatus.OK.value());
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}

		GenericResponse<List<InputWorkflowHistory>> response = new GenericResponse<>(true, "Records found", histories,
				HttpStatus.OK.value());

		return ResponseEntity.ok(response);
	}

	@GetMapping("/my-task/in-progress-count")
	public InProgressCountResponse getInProgressStatusCount() {
		return workflowService.getInProgressCount(utils.getUserIdFromAuthentication(),
				utils.getHouseIdFromAuthentication());
	}

}
