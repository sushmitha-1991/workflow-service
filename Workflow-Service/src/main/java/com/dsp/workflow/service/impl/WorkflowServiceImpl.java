package com.dsp.workflow.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.dsp.workflow.dto.ProcessStepwiseDocumentsDTO;
import com.dsp.workflow.dto.PublishRequestDTO;
import com.dsp.workflow.dto.TaskHistoryDTO;
import com.dsp.workflow.dto.TaskResponseDTO;
import com.dsp.workflow.dto.TaskVariableDto;
import com.dsp.workflow.entity.ApplicationInstance;
import com.dsp.workflow.entity.WorkflowHistory;
import com.dsp.workflow.exception.CamundaException;
import com.dsp.workflow.exception.ResourceNotFoundException;
import com.dsp.workflow.exception.TaskNotFoundException;
import com.dsp.workflow.mapper.WorkflowMapper;
import com.dsp.workflow.notesheet.dto.NotesheetDTO;
import com.dsp.workflow.notesheet.entity.Notesheet;
import com.dsp.workflow.notesheet.mapper.NotesheetMapper;
import com.dsp.workflow.notesheet.repository.NotesheetRepository;
import com.dsp.workflow.repository.ApplicationAuthorityRepository;
import com.dsp.workflow.repository.ApplicationHistoryRepository;
import com.dsp.workflow.repository.ApplicationInstanceRepository;
import com.dsp.workflow.repository.TaskRepository;
import com.dsp.workflow.repository.WorkflowHistoryRepository;
import com.dsp.workflow.service.WorkflowService;
import com.dsp.workflow.util.Utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import spinjar.com.fasterxml.jackson.core.JsonProcessingException;
import spinjar.com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WorkflowServiceImpl implements WorkflowService {

	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private NotesheetRepository notesheetRepository;

	@Autowired
	private ApplicationAuthorityRepository applicationAuthorityRepository;

	@Autowired
	private ApplicationInstanceRepository applicationInstanceRepository;

	@Autowired
	private ApplicationHistoryRepository applicationHistoryRepository;

	@Autowired
	private WorkflowHistoryRepository workflowHistoryRepository;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private Utils utils;

	private WorkflowMapper workflowMapper = new WorkflowMapper();

	@PersistenceContext
	private EntityManager entityManager;

	@Value("${camunda.bpm.rest.url}")
	private String camundaRestUrl;

	@Value("${camunda.bpm.admin-user.id}")
	private String camundaAdminId;

	@Value("${camunda.bpm.admin-user.password}")
	private String camundaAdminPassword;

	@Autowired
	private RestTemplate restTemplate;

//	@Override
//	public String startProcessInstance(String workflowId) {
//		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(workflowId);
//		return processInstance.getProcessInstanceId();
//	}

//	@Override
//	public String getTaskIdByProcessInstanceId(String processInstanceId) {
//		Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
//		return (task != null) ? task.getId() : null;
//	}

	@Override
	public String startProcessInstance(String workflowId) {
		String url = camundaRestUrl + "/process-definition/key/" + workflowId + "/start";
		Map<String, Object> requestBody = new HashMap<>();

		try {
			// Specify the type of response expected
			ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST,
					new HttpEntity<>(requestBody), new ParameterizedTypeReference<Map<String, Object>>() {
					});

			Map<String, Object> responseBody = response.getBody();
			return responseBody != null ? responseBody.get("id").toString() : null;

		} catch (HttpClientErrorException.NotFound e) {
			// Handle 404 Not Found
			String errorMessage = "No matching process definition found for workflow ID: " + workflowId;
			throw new CamundaException(errorMessage, e);

		} catch (HttpClientErrorException e) {
			String errorMessage = "Error while starting process instance: " + e.getResponseBodyAsString();
			throw new CamundaException(errorMessage, e);
		}
	}

	@Override
	public String getTaskIdByProcessInstanceId(String processInstanceId) {
		String url = camundaRestUrl + "/task?processInstanceId=" + processInstanceId;
		ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(url, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<Map<String, Object>>>() {
				});
		List<Map<String, Object>> tasks = response.getBody();
		return (tasks != null && !tasks.isEmpty()) ? tasks.get(0).get("id").toString() : null;
	}

//	@Transactional
//	public String completeTask(String taskId, Map<String, Object> variables) {
//		// Fetch the task
//		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
//		if (task != null) {
//			// Complete the task
//			taskService.complete(taskId, variables);
//
//			// Create a new Notesheet entity using the mapper
//			Notesheet notesheet = NotesheetMapper.fromVariables(variables, taskId);
//			notesheetRepository.save(notesheet);
//			// Create and save ApplicationAuthority entity using the mapper
//			ApplicationAuthority applicationAuthority = ApplicationAuthorityMapper.fromVariables(variables,
//					notesheet.getApplicationHistoryId());
//			applicationAuthorityRepository.save(applicationAuthority);
//
//			// Create and save ApplicationHistory entity using the mapper
//			ApplicationHistory applicationHistory = ApplicationHistoryMapper.fromVariables(variables, taskId);
//			applicationHistoryRepository.save(applicationHistory);
//
//			// Fetch the next task ID if available
//			Task nextTask = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
//			String nextTaskId = (nextTask != null) ? nextTask.getId() : null;
//
//			return nextTaskId;
//		} else {
//			return null;
//		}
//	}

	@Transactional
	public String completeTask(String taskId, Map<String, Object> variables,
			List<ProcessStepwiseDocumentsDTO> documentList) {
		try {
			String url = camundaRestUrl + "/task/" + taskId + "/complete";
			HttpHeaders headers = new HttpHeaders();
			headers.setBasicAuth(camundaAdminId, camundaAdminPassword);
			headers.setContentType(MediaType.APPLICATION_JSON);

			// Convert variables to a format expected by Camunda REST API
			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("variables", formatVariablesForCamunda(variables));

			// Create an HttpEntity with headers and body
			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

			// Execute the REST API call to complete the task
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

			String userName = utils.getUserFullNameFromAuthentication();
			Long senderUserId = ((Number) variables.get("senderUserId")).longValue();
			Long receiverUserId = ((Number) variables.get("receiverUserId")).longValue();
			Long applicationInstanceId = ((Number) variables.get("applicationInstanceId")).longValue();
			String processInstanceId = (String) variables.get("processInstanceId");
			String application_no = (String) variables.get("application_no");
			String actionTaken = (String) variables.get("actionTaken");
			String note = (String) variables.get("note");
			String statusKey = application_no + "." + actionTaken + ".PRESTATUS";
			String initalStatusKey = application_no + "." + actionTaken + ".POSTSTATUS";
			String receiverUserName = (String) variables.get("receiverUserName");
			String status = (String) variables.get("status");

			LocalDateTime actionTakenOn = LocalDateTime.now(); // Assuming the action is taken now
			// Check if the task was successfully completed
			if (response.getStatusCode().is2xxSuccessful()) {
				// Check if the stepType is "INIT"
				String stepType = (String) variables.get("stepType");
				if ("INIT".equals(stepType)) {

					// Entry in Notesheet Table
					Notesheet notesheet = NotesheetMapper.fromVariables(variables, taskId, documentList);
					notesheetRepository.save(notesheet);

					// First entry with status = "submitted"
					WorkflowHistory firstEntry = WorkflowHistory.builder().processInstanceId(processInstanceId)
							.subModuleId(application_no) // Set if applicable
							.applicationInstanceId(applicationInstanceId).actionTakenBy(senderUserId)
							.actionTakenByName(userName)
							// statusKey - appno+acc+prestatus
							.actionTaken(actionTaken).actionTakenOn(LocalDateTime.now()).status(status)
							.statusKey(statusKey).pendingWith(senderUserId).pendingWithName(userName)
							.pendingStartDate(LocalDateTime.now()).initalStatusKey(statusKey).note(note).build();
					workflowHistoryRepository.save(firstEntry); // Assuming you have a repository

					// Second entry, if needed, for the next task or status
					WorkflowHistory secondEntry = WorkflowHistory.builder().processInstanceId(processInstanceId)
							.subModuleId(application_no).applicationInstanceId(applicationInstanceId).actionTaken(null)
							.actionTakenBy(null).actionTakenByName(null).actionTakenOn(null).status(null)
							.statusKey(null).pendingWith(receiverUserId).pendingWithName(receiverUserName)
							.pendingStartDate(LocalDateTime.now()).initalStatusKey(initalStatusKey).note(note).build();
					workflowHistoryRepository.save(secondEntry);

				} else if ("INTER".equals(stepType) || "FINAL".equals(stepType)) {
					updateApplicationInstance(applicationInstanceId, processInstanceId, taskId, status);

					Notesheet notesheet = NotesheetMapper.fromVariables(variables, taskId, documentList);
					notesheetRepository.save(notesheet);
					workflowHistoryRepository.updateWorkflowHistoryForStepType(processInstanceId, applicationInstanceId,
							senderUserId, userName, actionTakenOn, status, statusKey);

					// Run update query if stepType is INTER or FINAL
					if ("INTER".equals(stepType)) {

						updateApplicationInstance(applicationInstanceId, processInstanceId, taskId, status);

						WorkflowHistory entry = WorkflowHistory.builder().processInstanceId(processInstanceId)
								.subModuleId(application_no).applicationInstanceId(applicationInstanceId)
								.actionTakenBy(null).actionTakenByName(null).actionTakenOn(null).status(null)
								.statusKey(null).pendingWith(receiverUserId).pendingWithName(receiverUserName)
								.pendingStartDate(LocalDateTime.now()).initalStatusKey(initalStatusKey).note(note)
								.build();
						workflowHistoryRepository.save(entry);
					}
				}

				// Fetch the next task ID using the process instance ID
				String nextTaskId = fetchNextTaskId(processInstanceId);
				return nextTaskId; // Return the next task ID instead of the current task ID
			} else {
				throw new RuntimeException("Failed to complete task with Camunda API");
			}
		} catch (Exception e) {
			throw new CamundaException("Error completing task with external Camunda API: " + e.getMessage(), e);
		}
	}

	// New method to fetch the next task ID using the process instance ID
	private String fetchNextTaskId(String processInstanceId) {
		// Construct the query URL for tasks by process instance ID
		String queryUrl = camundaRestUrl + "/task?processInstanceId=" + processInstanceId;

		// Execute the REST API call to get tasks
		ResponseEntity<List<TaskDto>> tasksResponse = restTemplate.exchange(queryUrl, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<TaskDto>>() {
				});

		List<TaskDto> tasks = tasksResponse.getBody();
		if (tasks != null && !tasks.isEmpty()) {
			// Return the ID of the first task in the list, which is considered the next
			// task
			return tasks.get(0).getId();
		}
		return null;
	}

	// Helper method to format the variables as Camunda expects
	private Map<String, Object> formatVariablesForCamunda(Map<String, Object> variables) {
		Map<String, Object> camundaVariables = new HashMap<>();

		variables.forEach((key, value) -> {
			if (value instanceof Map) {
				ObjectMapper objectMapper = new ObjectMapper();
				try {
					// Serialize the map to a JSON string
					String jsonValue = objectMapper.writeValueAsString(value);
					Map<String, Object> variableMap = new HashMap<>();
					variableMap.put("value", jsonValue);
					variableMap.put("type", "Json");
					camundaVariables.put(key, variableMap);
				} catch (JsonProcessingException e) {
					throw new RuntimeException("Error serializing map to JSON: " + key, e);
				}
			} else {
				Map<String, Object> variableMap = new HashMap<>();
				variableMap.put("value", value);
				variableMap.put("type", value.getClass().getSimpleName());
				camundaVariables.put(key, variableMap);
			}
		});

		return camundaVariables;
	}
//	public List<TaskResponseDTO> getTasksByReceiverUserId(Long receiverUserId) {
//		try {
//			// Step 1: Retrieve tasks filtered by receiverUserId
//			String queryUrl = camundaRestUrl + "/task?processVariableName=receiverUserId&processVariableValue="
//					+ receiverUserId;
//			ResponseEntity<List<TaskDto>> response = restTemplate.exchange(queryUrl, HttpMethod.GET, null,
//					new ParameterizedTypeReference<List<TaskDto>>() {
//					});
//
//			// Check if tasks were successfully retrieved
//			if (response.getStatusCode().is2xxSuccessful()) {
//				List<TaskDto> taskDtos = response.getBody() != null ? response.getBody() : Collections.emptyList();
//
//				// Step 2: Filter tasks to include only those where receiverUserId matches
//				List<TaskResponseDTO> filteredTasks = taskDtos.stream().filter(taskDto -> {
//					// Retrieve the actual receiverUserId variable for the task
//					String variableUrl = camundaRestUrl + "/task/" + taskDto.getId() + "/variables/receiverUserId";
//					ResponseEntity<TaskVariableDto> variableResponse = restTemplate.getForEntity(variableUrl,
//							TaskVariableDto.class);
//
//					// Verify that receiverUserId matches
//					return variableResponse.getStatusCode().is2xxSuccessful() && variableResponse.getBody() != null
//							&& receiverUserId.equals(variableResponse.getBody().getValue());
//				}).map(taskDto -> {
//					// Step 3: Fetch applicationInstanceId variable if receiverUserId matches
//					String appInstanceUrl = camundaRestUrl + "/task/" + taskDto.getId()
//							+ "/variables/applicationInstanceId";
//					ResponseEntity<TaskVariableDto> appInstanceResponse = restTemplate.getForEntity(appInstanceUrl,
//							TaskVariableDto.class);
//
//					Long applicationInstanceId = null;
//					if (appInstanceResponse.getStatusCode().is2xxSuccessful()
//							&& appInstanceResponse.getBody() != null) {
//						applicationInstanceId = appInstanceResponse.getBody().getValue();
//					}
//
//					return new TaskResponseDTO(taskDto.getId(), taskDto.getName(), applicationInstanceId);
//				}).collect(Collectors.toList());
//
//				// Step 4: Check if any tasks were found; if not, throw a custom exception
//				if (filteredTasks.isEmpty()) {
//					throw new ResourceNotFoundException(
//							"No pending tasks found for the provided receiverUserId: " + receiverUserId);
//				}
//
//				return filteredTasks;
//			} else {
//				throw new ResourceNotFoundException("Failed to retrieve tasks: " + response.getStatusCode());
//			}
//		} catch (ResourceNotFoundException e) {
//			throw e; // Preserve custom exception for resource not found
//		} catch (Exception e) {
//			throw new RuntimeException("An unexpected error occurred while retrieving tasks: " + e.getMessage(), e);
//		}
//	}

	public List<TaskResponseDTO> getTasksByReceiverUserId(Long receiverUserId) {
		try {
			// Step 1: Retrieve tasks filtered by receiverUserId
			String queryUrl = camundaRestUrl + "/task?processVariableName=receiverUserId&processVariableValue="
					+ receiverUserId;
			ResponseEntity<List<TaskDto>> response = restTemplate.exchange(queryUrl, HttpMethod.GET, null,
					new ParameterizedTypeReference<List<TaskDto>>() {
					});

			if (response.getStatusCode().is2xxSuccessful()) {
				List<TaskDto> taskDtos = response.getBody() != null ? response.getBody() : Collections.emptyList();
				List<TaskResponseDTO> filteredTasks = new ArrayList<>();

				for (TaskDto taskDto : taskDtos) {
					String variableUrl = camundaRestUrl + "/task/" + taskDto.getId() + "/variables/receiverUserId";
					ResponseEntity<TaskVariableDto> variableResponse;

					try {
						// Attempt to fetch receiverUserId
						variableResponse = restTemplate.getForEntity(variableUrl, TaskVariableDto.class);

						if (variableResponse.getStatusCode().is2xxSuccessful() && variableResponse.getBody() != null
								&& receiverUserId.equals(variableResponse.getBody().getValue())) {

							// Step 3: Fetch applicationInstanceId variable
							String appInstanceUrl = camundaRestUrl + "/task/" + taskDto.getId()
									+ "/variables/applicationInstanceId";
							ResponseEntity<TaskVariableDto> appInstanceResponse = restTemplate
									.getForEntity(appInstanceUrl, TaskVariableDto.class);

							Long applicationInstanceId = null;
							if (appInstanceResponse.getStatusCode().is2xxSuccessful()
									&& appInstanceResponse.getBody() != null) {
								applicationInstanceId = appInstanceResponse.getBody().getValue();
							}

							String processInstanceId = taskDto.getProcessInstanceId();

							// Step 4: Fetch task details from the repository
							List<Object[]> taskDetails = taskRepository
									.findTasksWithDetailsByReceiverUserIdAndApplicationInstanceId(receiverUserId,
											applicationInstanceId);

							if (!taskDetails.isEmpty()) {
								Object[] details = taskDetails.get(0);

								TaskResponseDTO taskResponseDTO = TaskResponseDTO.builder()
										.applicationNo((String) details[0]).documentName((String) details[1])
										.date((Date) details[2]).status((String) details[3])
										.pendingAt((String) details[4]).lsNo((String) details[5])
										.ssNo((String) details[6]).refId((Long) details[7])
										.applicationId((Long) details[8]).taskId(taskDto.getId())
										.taskName(taskDto.getName()).processInstanceId(processInstanceId) // Add
																											// processInstanceId
										.applicationInstanceId(applicationInstanceId).build();
								filteredTasks.add(taskResponseDTO);
							} else {
								throw new TaskNotFoundException("No task details found for receiverUserId: "
										+ receiverUserId + " and applicationInstanceId: " + applicationInstanceId);
							}
						}
					} catch (HttpClientErrorException.NotFound ex) {
						// Handle the case where receiverUserId is missing for this task
						continue; // Skip to the next task
					}
				}

				if (filteredTasks.isEmpty()) {
					throw new TaskNotFoundException(
							"No pending tasks found for the provided receiverUserId: " + receiverUserId);
				}

				return filteredTasks;
			} else {
				throw new ResourceNotFoundException("Failed to retrieve tasks: " + response.getStatusCode());
			}
		} catch (TaskNotFoundException e) {
			throw e; // Preserve custom exception for resource not found
		} catch (Exception e) {
			throw new RuntimeException("An unexpected error occurred while retrieving tasks: " + e.getMessage(), e);
		}
	}

//	private void logTaskHistory(String taskId, String processInstanceId, Map<String, Object> variables,
//			String nextTaskId) {
//		TaskHistory taskHistory = new TaskHistory();
//		taskHistory.setTaskId(taskId);
//		taskHistory.setProcessInstanceId(processInstanceId);
//		taskHistory.setUserId((String) variables.get("userId"));
//		taskHistory.setUserName((String) variables.get("userName"));
//		taskHistory.setAction((String) variables.get("sendTo"));
//		taskHistory.setComments((String) variables.get("comments"));
////		taskHistory.setDocumentTitle((String) variables.get("documentTitle"));
////		taskHistory.setDocDesc((String) variables.get("docDesc"));
//		taskHistory.setTimestamp(new Date());
//		taskHistory.setStatus("Completed");
//		taskHistory.setNextTaskId(nextTaskId);
//
//		taskHistoryRepository.save(taskHistory);
//	}

//	public List<HistoricTaskInstance> getHistoricTasksByTaskId(String taskId) {
//		HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();
//
//		if (taskId != null) {
//			query.taskId(taskId);
//		}
//
//		return query.list();
//	}

	public List<TaskHistoryDTO> getTasksByProcessInstanceId(String processInstanceId) {
		try {
			// Construct the URL to fetch historic tasks for the specified process instance
			// ID
			String url = camundaRestUrl + "/history/task?processInstanceId=" + processInstanceId;

			// Create headers for basic authentication
			HttpHeaders headers = new HttpHeaders();
			headers.setBasicAuth(camundaAdminId, camundaAdminPassword);
			headers.setContentType(MediaType.APPLICATION_JSON);

			// Create an HttpEntity with headers
			HttpEntity<Void> entity = new HttpEntity<>(headers);

			// Make the GET request to Camunda's API
			ResponseEntity<List<HistoricTaskInstanceDto>> response = restTemplate.exchange(url, HttpMethod.GET, entity,
					new ParameterizedTypeReference<List<HistoricTaskInstanceDto>>() {
					});

			// Convert the response body to List<TaskHistoryDTO>
			if (response.getBody() != null) {
				return response.getBody().stream().map(workflowMapper::mapToTaskHistoryDTO)
						.collect(Collectors.toList());
			} else {
				return Collections.emptyList(); // Return an empty list if no tasks are found
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to retrieve task history: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean isDocumentApprovedForPublishing(String taskId) {
		// Fetch the task by taskId
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

		// Check if the task is valid
		if (task == null) {
			throw new IllegalArgumentException("Task not found for ID: " + taskId);
		}

		// Retrieve the process variable for 'isApprovedForPublishing'
		Boolean isApprovedForPublishing = (Boolean) runtimeService.getVariable(task.getProcessInstanceId(),
				"isApprovedForPublishing");

		// Return true if the document is approved
		return Boolean.TRUE.equals(isApprovedForPublishing);
	}

	@Override
	public String publishDocument(PublishRequestDTO requestDTO) {
		// Fetch the task
		Task task = taskService.createTaskQuery().taskId(requestDTO.getTaskId()).singleResult();

		if (task == null) {
			throw new IllegalArgumentException("Task not found for ID: " + requestDTO.getTaskId());
		}

		// Create variables to complete the task
		Map<String, Object> variables = new HashMap<>();
		variables.put("note", requestDTO.getNote());
		variables.put("senderRoleId", requestDTO.getSenderRoleId());
		variables.put("applicationInstanceId", requestDTO.getApplicationInstanceId());
		variables.put("actionTaken", requestDTO.getActionTaken());

		// Complete the task
		taskService.complete(requestDTO.getTaskId(), variables);

		// Return confirmation of the task completion (e.g., task id or a message)
		return "Task with ID: " + requestDTO.getTaskId() + " completed successfully.";
	}

	@Override
	public void saveNotesheet(NotesheetDTO notesheetDTO, String processInstanceId, String taskId) {
		// TODO Auto-generated method stub

	}

	public void updateApplicationInstance(long applicationInstanceId, String processInstanceId, String taskId,
			String status) {
		// Retrieve the application instance by ID
		Optional<ApplicationInstance> optionalInstance = applicationInstanceRepository.findById(applicationInstanceId);

		if (optionalInstance.isPresent()) {
			ApplicationInstance applicationInstance = optionalInstance.get();
			applicationInstance.setProcessInstanceId(processInstanceId);
			applicationInstance.setTaskId(taskId);
			applicationInstance.setCurrentStatus(status);
			// Save the updated instance
			applicationInstanceRepository.save(applicationInstance);
		} else {
			throw new ResourceNotFoundException("ApplicationInstance with ID " + applicationInstanceId + " not found.");
		}
	}

	@Override
	public WorkflowHistory getMostRecentWorkflowHistoryBySubModuleIdAndActionTakenBy(String subModuleId, Long userId) {
		return workflowHistoryRepository.findTopBySubModuleIdAndActionTakenByOrderByCreatedAtDesc(subModuleId, userId);
	}

	@Override
	public List<WorkflowHistory> getWorkflowHistoryBySubModuleIdAndActionTakenBy(String subModuleId,
			Long actionTakenBy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowHistory> getWorkflowHistoryByApplicationInstanceId(Long applicationInstanceId) {
		return workflowHistoryRepository.findByApplicationInstanceId(applicationInstanceId);
	}

	public void updateApplicationInstance(NotesheetDTO notesheetDTO, String processInstanceId, String taskId) {
		// Retrieve the application instance by ID
		Optional<ApplicationInstance> optionalInstance = applicationInstanceRepository
				.findById(notesheetDTO.getApplicationInstanceId());

		if (optionalInstance.isPresent()) {
			ApplicationInstance applicationInstance = optionalInstance.get();

			// Update fields
			applicationInstance.setProcessInstanceId(processInstanceId);
			applicationInstance.setTaskId(taskId);
			applicationInstance.setCurrentStatus("In Progress"); // Hardcoding to "Initiated"

			// Save the updated instance
			applicationInstanceRepository.save(applicationInstance);
		} else {
			throw new ResourceNotFoundException(
					"ApplicationInstance with ID " + notesheetDTO.getApplicationInstanceId() + " not found.");
		}
	}

}
