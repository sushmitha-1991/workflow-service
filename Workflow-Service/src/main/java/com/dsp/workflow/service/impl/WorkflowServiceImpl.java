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

import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.dsp.workflow.dto.DraftDataDTO;
import com.dsp.workflow.dto.ProcessStepwiseDocumentsDTO;
import com.dsp.workflow.dto.SerialNumberRequest;
import com.dsp.workflow.dto.SerialNumberResponse;
import com.dsp.workflow.dto.TaskHistoryDTO;
import com.dsp.workflow.dto.TaskQueryDTO;
import com.dsp.workflow.dto.TaskResponseDTO;
import com.dsp.workflow.dto.TaskVariableDto;
import com.dsp.workflow.dto.UserDetailsDTO;
import com.dsp.workflow.entity.ApplicationInstance;
import com.dsp.workflow.entity.DraftDocument;
import com.dsp.workflow.entity.WorkflowHistory;
import com.dsp.workflow.exception.CamundaException;
import com.dsp.workflow.exception.ResourceNotFoundException;
import com.dsp.workflow.exception.TaskNotFoundException;
import com.dsp.workflow.mapper.DocumentRowMapper;
import com.dsp.workflow.mapper.WorkflowMapper;
import com.dsp.workflow.notesheet.dto.NotesheetDTO;
import com.dsp.workflow.notesheet.entity.Notesheet;
import com.dsp.workflow.notesheet.mapper.NotesheetMapper;
import com.dsp.workflow.notesheet.repository.NotesheetRepository;
import com.dsp.workflow.repository.ApplicationInstanceRepository;
import com.dsp.workflow.repository.DraftDocumentRepository;
import com.dsp.workflow.repository.TaskRepository;
import com.dsp.workflow.repository.WorkflowHistoryRepository;
import com.dsp.workflow.service.WorkflowService;
import com.dsp.workflow.util.Utils;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import spinjar.com.fasterxml.jackson.core.JsonProcessingException;
import spinjar.com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WorkflowServiceImpl implements WorkflowService {

	@Autowired
	private NotesheetRepository notesheetRepository;

	@Autowired
	private ApplicationInstanceRepository applicationInstanceRepository;

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

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Autowired
	private DraftDocumentRepository draftDocumentRepository;

	@Value("${master.service.url}")
	private String masterServiceUrl;
	@Autowired
	private HttpServletRequest httpRequest;

	private final static String allDocumentsQuery = """
			SELECT applicationNo, documentName, "date", LSNo, SSNo, RSSNo, status, pendingAt, refId,
			        applicationId, applicationinstanceId, taskName, taskId, actionPerformed, formUrl, actionUrl, draftId, publishedUrl, ma.translate_flag, ma.download_flag
			FROM (
			     SELECT DISTINCT ON (ai.application_no)
			            ai.application_no AS applicationNo,
			            a.name AS documentName,
			            ai.document_date AS date,
			            ai.lok_sabha_session AS LSNo,
			            ai.lok_sabha_session_number AS SSNo,
			            ai.rajya_sabha_session AS RSSNo,
			            ai.current_status AS status,
			            CASE
			                WHEN ai.current_status = 'Approved' THEN NULL
			                ELSE CONCAT(wh.pending_with_name)
			            END AS pendingAt,
			            ai.ref_id AS refId,
			            ai.application_id AS applicationId,
			            ai.id AS applicationinstanceId,
			            'IN-PROCESS' AS taskName,
			            wh.next_task_id AS taskId,
			            'True' AS actionPerformed,
			            a.form_url AS formUrl,
			            wh.action_url AS actionUrl,
			            wh.draft_id as draftId,
			            a.published_url as publishedUrl
			     FROM workflow.workflow_history wh
			     JOIN master.application_instance ai ON wh.application_instance_id = ai.id
			     JOIN master.applications a ON ai.application_id = a.id
			     JOIN authentication.base_user u ON u.id = wh.pending_with
			     JOIN authentication.user_roles ur ON ur.user_id = u.id AND ur.is_active = true
			     JOIN authentication.roles r ON r.id = ur.role_id
			     JOIN master.offices o ON o.id = ur.office_id
			     WHERE wh.pending_with = :userId
			       AND wh.action_taken_by IS NULL

			     UNION

			     SELECT DISTINCT ON (ai.application_no)
			            ai.application_no AS applicationNo,
			            a.name AS documentName,
			            ai.document_date AS date,
			            ai.lok_sabha_session AS LSNo,
			            ai.lok_sabha_session_number AS SSNo,
			            ai.rajya_sabha_session AS RSSNo,
			            CASE
			                WHEN ai.current_status = 'Returned' THEN 'In Progress'
			                ELSE ai.current_status
			            END AS status,
			            CASE
			                WHEN ai.current_status = 'Approved' THEN NULL
			                ELSE CONCAT(wh.pending_with_name)
			            END AS pendingAt,
			            ai.ref_id AS refId,
			            ai.application_id AS applicationId,
			            ai.id AS applicationinstanceId,
			            'IN-PROCESS' AS taskName,
			            wh.next_task_id AS taskId,
			            'False' AS actionPerformed,
			            a.form_url AS formUrl,
			            wh.action_url AS actionUrl,
			            wh.draft_id as draftId,
			            a.published_url as publishedUrl
			     FROM workflow.workflow_history wh
			     JOIN master.application_instance ai ON wh.application_instance_id = ai.id
			     JOIN master.applications a ON ai.application_id = a.id
			     JOIN authentication.base_user u ON u.id = wh.pending_with
			     JOIN authentication.user_roles ur ON ur.user_id = u.id AND ur.is_active = true
			     JOIN authentication.roles r ON r.id = ur.role_id
			     JOIN master.offices o ON o.id = ur.office_id
			     WHERE wh.id IN (
			         SELECT MAX(wh2.id)
			         FROM workflow.workflow_history wh2
			         WHERE wh2.application_instance_id IN (
			             SELECT wh3.application_instance_id
			             FROM workflow.workflow_history wh3
			             WHERE wh3.pending_with IS NOT NULL
			               AND wh3.action_taken_by = :userId
			               AND wh3.application_instance_id NOT IN (
			                   SELECT wh1.application_instance_id
			                   FROM workflow.workflow_history wh1
			                   WHERE wh1.action_taken_by IS NULL
			                     AND wh1.pending_with = :userId
			               )
			         )
			         GROUP BY wh2.application_instance_id
			     )

			     UNION

			     SELECT DISTINCT ON (ii.application_no)
			            ii.application_no AS applicationNo,
			            a.name AS documentName,
			            ii.document_date AS date,
			            ii.lok_sabha_session AS LSNo,
			            ii.lok_sabha_session_number AS SSNo,
			            ii.rajya_sabha_session AS RSSNo,
			            ii.current_status AS status,
			            ii.pending_at AS pendingAt,
			            ii.ref_id AS refId,
			            ii.application_id AS applicationId,
			            ii.id AS applicationinstanceId,
			            'IN-PROCESS' AS taskName,
			            NULL AS taskId,
			            'False' AS actionPerformed,
			            a.form_url AS formUrl,
			            NULL AS actionUrl,
			            NULL as draftId,
			            a.published_url as publishedUrl
			     FROM master.input_instance ii
			     JOIN master.applications a ON a.id = ii.application_id
			     JOIN authentication.base_user u ON u.id = ii.created_by
			     WHERE ii.created_by = :userId

			     UNION

				 SELECT DISTINCT ON (dd.application_no)
			            dd.application_no AS applicationNo,
			            a.name AS documentName,
			            NULL AS date,
			            dd.lok_sabha_session AS LSNo,
			            dd.lok_sabha_session_number AS SSNo,
			            dd.rajya_sabha_session AS RSSNo,
			            wh.status AS status,
			            wh.pending_with_name AS pendingAt,
			            NULL AS refId,
			            dd.application_id AS applicationId,
			            NULL AS applicationinstanceId,
			            'IN-PROCESS' AS taskName,
			            NULL AS taskId,
			            'True' AS actionPerformed,
			            NULL AS formUrl,
			            NULL AS actionUrl,
			            wh.draft_id as draftId,
			            a.published_url as publishedUrl
			     FROM workflow.workflow_history wh
			     LEFT JOIN workflow.drafted_document dd ON dd.id = wh.draft_id
			     LEFT JOIN master.applications a ON a.id = dd.application_id
			     left JOIN authentication.base_user u ON u.id = wh.pending_with
			     WHERE wh.pending_with = :userId
			 ) A
			 LEFT JOIN master.applications ma on ma.id = A.applicationId
			""";

	private final static String orderByClause = """
			ORDER BY "date" DESC
			""";
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
			List<ProcessStepwiseDocumentsDTO> documentList, String actionUrl) {
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

			// fetch next task id
			String nextTaskId = fetchNextTaskId(processInstanceId);

			// Check if the task was successfully completed
			if (response.getStatusCode().is2xxSuccessful()) {

				// Check if the stepType is "INIT"
				String stepType = (String) variables.get("stepType");
				if ("INIT".equals(stepType)) {

					// Entry to Notesheet Table
					Notesheet notesheet = NotesheetMapper.fromVariables(variables, taskId, documentList);
					notesheetRepository.save(notesheet);

					// First entry with status = "submitted"
					WorkflowHistory firstEntry = WorkflowHistory.builder().processInstanceId(processInstanceId)
							.subModuleId(application_no).applicationInstanceId(applicationInstanceId)
							.actionTakenBy(senderUserId).actionTakenByName(userName).actionTaken(actionTaken)
							.actionTakenOn(LocalDateTime.now()).status(status).statusKey(statusKey)
							.pendingWith(senderUserId).pendingWithName(userName).pendingStartDate(LocalDateTime.now())
							.initalStatusKey(statusKey).note(note).currentTaskId(taskId).build();
					workflowHistoryRepository.save(firstEntry);

					// Second entry, if needed, for the next task or status
					WorkflowHistory secondEntry = WorkflowHistory.builder().processInstanceId(processInstanceId)
							.subModuleId(application_no).applicationInstanceId(applicationInstanceId).actionTaken(null)
							.actionTakenBy(null).actionTakenByName(null).actionTakenOn(null).status(null)
							.statusKey(null).pendingWith(receiverUserId).pendingWithName(receiverUserName)
							.pendingStartDate(LocalDateTime.now()).initalStatusKey(initalStatusKey).note(note)
							.nextTaskId(nextTaskId).actionUrl(actionUrl).build();
					workflowHistoryRepository.save(secondEntry);

				} else if ("INTER".equals(stepType) || "FINAL".equals(stepType)) {
					updateApplicationInstance(applicationInstanceId, processInstanceId, taskId, status);

					// Entry to Notesheet Table
					Notesheet notesheet = NotesheetMapper.fromVariables(variables, taskId, documentList);
					notesheetRepository.save(notesheet);

					workflowHistoryRepository.updateWorkflowHistoryForStepType(processInstanceId, applicationInstanceId,
							senderUserId, userName, actionTakenOn, status, statusKey, taskId);

					// Run update query if stepType is INTER or FINAL
					if ("INTER".equals(stepType)) {

						updateApplicationInstance(applicationInstanceId, processInstanceId, taskId, status);

						WorkflowHistory entry = WorkflowHistory.builder().processInstanceId(processInstanceId)
								.subModuleId(application_no).applicationInstanceId(applicationInstanceId)
								.actionTakenBy(null).actionTakenByName(null).actionTakenOn(null).status(null)
								.statusKey(null).pendingWith(receiverUserId).pendingWithName(receiverUserName)
								.pendingStartDate(LocalDateTime.now()).initalStatusKey(initalStatusKey).note(note)
								.nextTaskId(nextTaskId).actionUrl(actionUrl).build();
						workflowHistoryRepository.save(entry);
					}
				}

				if (nextTaskId == null) {
					return "Workflow has ended";
				}
				return nextTaskId;
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

	@Override
	public List<Map<String, Object>> getallDocumentsTab(Long userId) {
		return workflowHistoryRepository.findAllDocumentsForUser(userId).stream().map(row -> {
			Map<String, Object> rowMap = new HashMap<>();
			rowMap.put("applicationNo", row[0]);
			rowMap.put("documentName", row[1]);
			rowMap.put("date", row[2] != null ? row[2].toString() : null); // Handle NULL date
			rowMap.put("lsNo", row[3]);
			rowMap.put("ssNo", row[4]);
			rowMap.put("rssNo", row[5]);
			rowMap.put("status", row[6]);
			rowMap.put("pendingAt", row[7]);
			rowMap.put("refId", row[8]);
			rowMap.put("applicationId", row[9]);
			rowMap.put("applicationInstanceId", row[10]);
			rowMap.put("taskName", row[11]);
			rowMap.put("taskId", row[12]);
			rowMap.put("actionPerformed", row[13]);
			rowMap.put("formUrl", row[14]);
			rowMap.put("actionUrl", row[15]);
			rowMap.put("translateFlag", row[16]);
			rowMap.put("downloadFlag", row[17]);

			return rowMap;
		}).collect(Collectors.toList());
	}

	@Override
	public List<Map<String, Object>> getallApprovedTab(Long userId) {
		return workflowHistoryRepository.findAllApprovedDocumentsForUser(userId).stream().map(row -> {
			Map<String, Object> rowMap = new HashMap<>();
			rowMap.put("applicationNo", row[0]);
			rowMap.put("documentName", row[1]);
			rowMap.put("date", row[2].toString()); // Format date as needed
			rowMap.put("lsNo", row[3]);
			rowMap.put("ssNo", row[4]);
			rowMap.put("status", row[5]);
			rowMap.put("pendingAt", row[6]);
			rowMap.put("refId", row[7]);
			rowMap.put("applicationId", row[8]);
			rowMap.put("applicationInstanceId", row[9]);
			return rowMap;
		}).collect(Collectors.toList());
	}

	@Override
	public List<Map<String, Object>> filterAllDocumentsTab(Long userId, TaskQueryDTO queryDto) {

		StringBuffer qry = new StringBuffer(allDocumentsQuery).append(" where 1 = 1 ");

		MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("userId", userId);

		if (queryDto.getApplicationId() != null) {
//	            qry = qry.append(" and applicationNo = ").append(queryDto.getApplicationId());
			qry = qry.append(" and applicationId = :applicationId ");
			parameters.addValue("applicationId", queryDto.getApplicationId());
		}

		if (!Utils.isEmpty(queryDto.getLsNo())) {
//	            qry = qry.append(" and LSNo = '").append(queryDto.getLsNo()).append("'");
			qry = qry.append(" and LSNo = :LsNo ");
			parameters.addValue("LsNo", queryDto.getLsNo());
		}

		if (!Utils.isEmpty(queryDto.getLsSessionNo())) {
//	            qry = qry.append(" and SSNo = '").append(queryDto.getLsSessionNo()).append("'");
			qry = qry.append(" and SSNo = :LsSessionNo ");
			parameters.addValue("LsSessionNo", queryDto.getLsSessionNo());
		}

		if (!Utils.isEmpty(queryDto.getRsSessionNo())) {
//	            qry = qry.append(" and RSSNo = '").append(queryDto.getRsSessionNo()).append("'");
			qry = qry.append(" and RSSNo = :RsSessionNo ");
			parameters.addValue("RsSessionNo", queryDto.getRsSessionNo());
		}

		if (!Utils.isEmpty(queryDto.getPendingAt())) {
			qry = qry.append(" and pendingAt like '%").append(queryDto.getPendingAt()).append("%'");
		}

		qry.append(orderByClause);

		List<DocumentRowMapper> results = jdbcTemplate.query(qry.toString(), parameters,
				new BeanPropertyRowMapper<>(DocumentRowMapper.class));

		return results.stream().map(row -> {
			Map<String, Object> rowMap = new HashMap<>();
			rowMap.put("applicationNo", row.getApplicationNo());
			rowMap.put("documentName", row.getDocumentName());
			rowMap.put("date", row.getDate() != null ? row.getDate().toString() : null); // Handle NULL date
			rowMap.put("lsNo", row.getLSNo());
			rowMap.put("ssNo", row.getSSNo());
			rowMap.put("rssNo", row.getRSSNo());
			rowMap.put("status", row.getStatus());
			rowMap.put("pendingAt", row.getPendingAt());
			rowMap.put("refId", row.getRefId());
			rowMap.put("applicationId", row.getApplicationId());
			rowMap.put("applicationInstanceId", row.getApplicationInstanceId());
			rowMap.put("taskName", row.getTaskName());
			rowMap.put("taskId", row.getTaskId());
			rowMap.put("actionPerformed", row.getActionPerformed());
			rowMap.put("formUrl", row.getFormUrl());
			rowMap.put("actionUrl", row.getActionUrl());
			rowMap.put("draftId", row.getDraftId());
			rowMap.put("publishedUrl", row.getPublishedUrl());
			rowMap.put("translateFlag", row.getTranslateFlag());
			rowMap.put("downloadFlag", row.getDownloadFlag());
			return rowMap;
		}).collect(Collectors.toList());

	}

	@Transactional
	public List<Map<String, Object>> saveAsDraft(Long userId, DraftDataDTO draftDataDTO) {
		// Convert the incoming JSON data (draftDataDTO.getData()) to a JsonNode
		String userName = utils.getUserFullNameFromAuthentication();

		Long houseId = null;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetailsDTO) {
			houseId = ((UserDetailsDTO) principal).getHouseId();
		}
		SerialNumberRequest serialNumberRequest = new SerialNumberRequest();

		serialNumberRequest.setAppId(draftDataDTO.getApplicationId());
		if (houseId == 1) {
			serialNumberRequest.setSessionId(draftDataDTO.getRajyaSabhaSession());
		}
		if (houseId == 2) {
			serialNumberRequest.setSessionId(draftDataDTO.getLokSabhaSessionNumber());
		}
		System.out.println("### serialNumberRequest - " + serialNumberRequest);
		SerialNumberResponse applicationNo = generateApplicationNo(serialNumberRequest);

		System.out.println("### applicationNo - " + applicationNo);

		JsonNode draftData = draftDataDTO.getData();

		// Create a new DraftDocument entity and set the draftData field
		DraftDocument draftDocument = new DraftDocument();
		draftDocument.setDraftData(draftData);
		draftDocument.setApplicationId(draftDataDTO.getApplicationId());
		draftDocument.setApplicationNo(applicationNo.getId());
		draftDocument.setLokSabhaSession(String.valueOf(draftDataDTO.getLokSabhaSession()));
		draftDocument.setLokSabhaSessionNumber(String.valueOf(draftDataDTO.getLokSabhaSessionNumber()));
		draftDocument.setRajyaSabhaSession(String.valueOf(draftDataDTO.getRajyaSabhaSession()));

		// Save the draft document to the database
		draftDocumentRepository.save(draftDocument);

		// Insert an entry to Workflow History (no result expected from this query)
		draftDocumentRepository.saveIntoWorkflowHistory(draftDataDTO.getApplicationId(), userId, userName,
				draftDocument.getId());

		// Prepare the response with the saved data
		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("id", draftDocument.getId());
		responseMap.put("draftData", draftDocument.getDraftData().toString());
		responseMap.put("applicationId", draftDocument.getApplicationId());
		result.add(responseMap);

		return result;
	}

	// Generate Application No Functionality
	public SerialNumberResponse generateApplicationNo(SerialNumberRequest request) {
		String url = masterServiceUrl + "/serial-number";
		String token = getTokenFromRequest();
		if (token == null) {
			throw new IllegalStateException("Authorization token is missing");
		}
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + token);
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<SerialNumberRequest> entity = new HttpEntity<>(request, headers);

		try {
			// RestTemplate restTemplate = new RestTemplate();
			return restTemplate.postForObject(url, entity, SerialNumberResponse.class);
		} catch (Exception e) {
			System.err.println("Error during API call: " + e.getMessage());
			throw e;
		}
	}

	public String getTokenFromRequest() {
		String authorizationHeader = httpRequest.getHeader("Authorization");
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			return authorizationHeader.substring(7);
		}
		return null;
	}

	public List<Map<String, Object>> getSaveAsDraft(Long draftId) {
		// Call the repository method to fetch the draft data
		List<Object[]> draftDataList = draftDocumentRepository.findDraftDataByUserAndApplication(draftId);

		List<Map<String, Object>> result = new ArrayList<>();

		// If data is found, map the fields to a structured response
		if (!draftDataList.isEmpty()) {
			for (Object[] draftDataRow : draftDataList) {
				Map<String, Object> responseMap = new HashMap<>();
				responseMap.put("draftData", draftDataRow[0]); // draftData (JsonNode)
				responseMap.put("applicationNo", draftDataRow[1]); // applicationNo
				responseMap.put("lokSabhaSession", draftDataRow[2]); // lokSabhaSession
				responseMap.put("lokSabhaSessionNumber", draftDataRow[3]); // lokSabhaSessionNumber
				responseMap.put("rajyaSabhaSession", draftDataRow[4]); // rajyaSabhaSession
				result.add(responseMap);
			}
		}

		return result;
	}

}
