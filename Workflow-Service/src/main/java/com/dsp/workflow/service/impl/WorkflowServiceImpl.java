package com.dsp.workflow.service.impl;

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

import com.dsp.workflow.dto.TaskResponseDTO;
import com.dsp.workflow.notesheet.entity.Notesheet;
import com.dsp.workflow.notesheet.mapper.NotesheetMapper;
import com.dsp.workflow.notesheet.repository.NotesheetRepository;
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

	@Override
	public String startProcessInstance(String workflowId) {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(workflowId);
		return processInstance.getProcessInstanceId();
	}

	@Override
	public String completeTask(String taskId, Map<String, Object> variables) {
		// Fetch the task
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		if (task != null) {
			// Complete the task
			taskService.complete(taskId, variables);

			// Create a new Notesheet entity using the mapper
			Notesheet notesheet = NotesheetMapper.fromVariables(variables, taskId);

			// Save the Notesheet entity
			notesheetRepository.save(notesheet);

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

}
