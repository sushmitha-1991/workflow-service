package com.dsp.workflow.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dsp.workflow.dto.PublishRequestDTO;
import com.dsp.workflow.dto.TaskHistoryDTO;
import com.dsp.workflow.dto.TaskResponseDTO;
import com.dsp.workflow.entity.ApplicationAuthority;
import com.dsp.workflow.entity.ApplicationHistory;
import com.dsp.workflow.mapper.WorkflowMapper;
import com.dsp.workflow.notesheet.dto.NotesheetDTO;
import com.dsp.workflow.notesheet.entity.Notesheet;
import com.dsp.workflow.notesheet.mapper.ApplicationAuthorityMapper;
import com.dsp.workflow.notesheet.mapper.ApplicationHistoryMapper;
import com.dsp.workflow.notesheet.mapper.NotesheetMapper;
import com.dsp.workflow.notesheet.repository.NotesheetRepository;
import com.dsp.workflow.repository.ApplicationAuthorityRepository;
import com.dsp.workflow.repository.ApplicationHistoryRepository;
import com.dsp.workflow.service.WorkflowService;

@Service
public class WorkflowServiceImpl implements WorkflowService {

	@Value("${camunda.url}")
	private String camundaUrl;

	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private NotesheetRepository notesheetRepository;

	@Autowired
	private HistoryService historyService;

	@Autowired
	private ApplicationAuthorityRepository applicationAuthorityRepository;

	@Autowired
	private ApplicationHistoryRepository applicationHistoryRepository;

	@Override
	public String startProcessInstance(String workflowId) {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(workflowId);
		return processInstance.getProcessInstanceId();
	}

	private WorkflowMapper workflowMapper = new WorkflowMapper();

//	@Override
//	public void saveNotesheet(NotesheetDTO notesheetDTO, String processInstanceId, String taskId) {
//		// Convert NotesheetDTO to Notesheet entity
//		Notesheet notesheet = Notesheet.builder().note(notesheetDTO.getNote())
//				.receiverRoleId(notesheetDTO.getReceiverRoleId()).receiverUserId(notesheetDTO.getReceiverUserId())
//				.senderRoleId(notesheetDTO.getSenderRoleId()).historyId(taskId) // Assuming the taskId is used as
//																				// historyId
//				.applicationInstanceId(notesheetDTO.getApplicationInstanceId())
//				.actionTaken(notesheetDTO.getActionTaken()).build();
//		notesheet.setProcessInstanceId(processInstanceId);
//
//		// Save the Notesheet entity to the database
//		notesheetRepository.save(notesheet);
//	}

	@Override
	public String completeTask(String taskId, Map<String, Object> variables) {
		// Fetch the task
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

		if (task != null) {
			// Complete the task
			taskService.complete(taskId, variables);

			// Create a new Notesheet entity using the mapper
			Notesheet notesheet = NotesheetMapper.fromVariables(variables, taskId);
			notesheetRepository.save(notesheet);

			// Create and save ApplicationAuthority entity using the mapper
			ApplicationAuthority applicationAuthority = ApplicationAuthorityMapper.fromVariables(variables,
					notesheet.getApplicationHistoryId());
			applicationAuthorityRepository.save(applicationAuthority);

			// Create and save ApplicationHistory entity using the mapper
			ApplicationHistory applicationHistory = ApplicationHistoryMapper.fromVariables(variables, taskId);
			applicationHistoryRepository.save(applicationHistory);

			// Fetch the next task ID if available
			Task nextTask = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
			String nextTaskId = (nextTask != null) ? nextTask.getId() : null;

			return nextTaskId;
		} else {
			return null;
		}
	}

	public List<TaskResponseDTO> getPendingTasksByReceiverUserId(Integer receiverUserId) {
		// Query Camunda for all tasks
		List<Task> tasks = taskService.createTaskQuery().list();

		// Filter tasks by senderRoleId
		List<TaskResponseDTO> filteredTasks = tasks.stream().filter(task -> {
			// Get the senderRoleId variable for the current task
			Integer taskReceiverUserId = (Integer) runtimeService.getVariable(task.getProcessInstanceId(),
					"receiverUserId");
			// Return true if the senderRoleId matches
			return receiverUserId.equals(taskReceiverUserId);
		}).map(task -> new TaskResponseDTO(task.getId(), task.getName(), task.getAssignee(), task.getCreateTime()))
				.collect(Collectors.toList());

		return filteredTasks;
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

	@Override
	public String getTaskIdByProcessInstanceId(String processInstanceId) {
		Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
		return (task != null) ? task.getId() : null;
	}

	public List<HistoricTaskInstance> getHistoricTasksByTaskId(String taskId) {
		HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

		if (taskId != null) {
			query.taskId(taskId);
		}

		return query.list();
	}

	public List<TaskHistoryDTO> getTasksByProcessInstanceId(String processInstanceId) {
		List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
				.processInstanceId(processInstanceId).orderByHistoricTaskInstanceEndTime().asc().list();

		return historicTaskInstances.stream().map(workflowMapper::mapToTaskHistoryDTO).collect(Collectors.toList());
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

}
