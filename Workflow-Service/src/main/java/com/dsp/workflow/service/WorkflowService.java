package com.dsp.workflow.service;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.history.HistoricTaskInstance;

import com.dsp.workflow.dto.PublishRequestDTO;
import com.dsp.workflow.dto.TaskHistoryDTO;
import com.dsp.workflow.dto.TaskResponseDTO;
import com.dsp.workflow.notesheet.dto.NotesheetDTO;

public interface WorkflowService {

	/**
	 * Starts a process instance by the given process key and variables.
	 * 
	 * @param workflowId The key of the process to start.
	 * @param variables  The variables to pass to the process instance.
	 * @return The process instance ID.
	 */
	String startProcessInstance(String workflowId);

	/**
	 * Completes a task with the given task ID and variables.
	 * 
	 * @param taskId       The ID of the task to complete.
	 * @param variables    The variables to pass for task completion.
	 * @param senderRoleId
	 * @return The new task ID, or null if no new task is created.
	 */
	String completeTask(String taskId, Map<String, Object> variables);

	/**
	 * Retrieves a task ID by process instance ID.
	 * 
	 * @param processInstanceId The ID of the process instance.
	 * @return The task ID associated with the process instance, or null if not
	 *         found.
	 */
	String getTaskIdByProcessInstanceId(String processInstanceId);

	List<TaskHistoryDTO> getTasksByProcessInstanceId(String processInstanceId);

	boolean isDocumentApprovedForPublishing(String taskId);

	String publishDocument(PublishRequestDTO requestDTO);

	/**
	 * @param notesheetDTO
	 * @param processInstanceId
	 * @param taskId
	 */
	void saveNotesheet(NotesheetDTO notesheetDTO, String processInstanceId, String taskId);

	/**
	 * @param notesheetDTO
	 * @param processInstanceId
	 * @param taskId
	 * @param senderRoleId
	 */
	void insertApplicationInstance(NotesheetDTO notesheetDTO, String processInstanceId, String taskId,
			Long senderRoleId);

	/**
	 * @param receiverUserId
	 * @return
	 */
	List<TaskResponseDTO> getTasksByReceiverUserId(Integer receiverUserId);

}
