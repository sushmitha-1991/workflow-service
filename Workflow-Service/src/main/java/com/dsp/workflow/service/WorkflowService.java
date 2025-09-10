package com.dsp.workflow.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.dsp.workflow.dto.DraftDataDTO;
import com.dsp.workflow.dto.InProgressCountResponse;
import com.dsp.workflow.dto.ProcessStepwiseDocumentsDTO;
import com.dsp.workflow.dto.TaskHistoryDTO;
import com.dsp.workflow.dto.TaskQueryDTO;
import com.dsp.workflow.dto.TaskResponseDTO;
import com.dsp.workflow.entity.WorkflowHistory;
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
	 * @param taskId              The ID of the task to complete.
	 * @param variables           The variables to pass for task completion.
	 * @param list
	 * @param actionUrl
	 * @param note
	 * @param translationInputDTO
	 * @param translationFlag
	 * @param senderRoleId
	 * @return The new task ID, or null if no new task is created.
	 */
	String completeTask(String taskId, Map<String, Object> variables, List<ProcessStepwiseDocumentsDTO> list,
			String actionUrl, String note);

	/**
	 * Retrieves a task ID by process instance ID.
	 * 
	 * @param processInstanceId The ID of the process instance.
	 * @return The task ID associated with the process instance, or null if not
	 *         found.
	 */
	String getTaskIdByProcessInstanceId(String processInstanceId);

	List<TaskHistoryDTO> getTasksByProcessInstanceId(String processInstanceId);

	/**
	 * @param notesheetDTO
	 * @param processInstanceId
	 * @param taskId
	 */
	void saveNotesheet(NotesheetDTO notesheetDTO, String processInstanceId, String taskId);

	/**
	 * @param receiverUserId
	 * @return
	 */
	List<TaskResponseDTO> getTasksByReceiverUserId(Long receiverUserId);

	/**
	 * @param processInstanceId
	 * @param taskId
	 * @param string
	 * @param b
	 */
	void updateApplicationInstance(long applicationInstanceId, String processInstanceId, String taskId, String string);

	/**
	 * @param subModuleId
	 * @param actionTakenBy
	 * @return
	 */
	List<WorkflowHistory> getWorkflowHistoryBySubModuleIdAndActionTakenBy(String subModuleId, Long actionTakenBy);

	/**
	 * @param subModuleId
	 * @param userId
	 * @return
	 */
	WorkflowHistory getMostRecentWorkflowHistoryBySubModuleIdAndActionTakenBy(String subModuleId, Long userId);

	/**
	 * @param applicationInstanceId
	 * @return
	 */
	List<WorkflowHistory> getWorkflowHistoryByApplicationInstanceId(Long applicationInstanceId);

	/**
	 * @param notesheetDTO
	 * @param processInstanceId
	 * @param taskId
	 */
	void updateApplicationInstance(NotesheetDTO notesheetDTO, String processInstanceId, String taskId);

	/**
	 * @param userId
	 * @return
	 */
	List<Map<String, Object>> getallDocumentsTab(Long userId);

	/**
	 * @param userId
	 * @return
	 */
	List<Map<String, Object>> getallApprovedTab(Long userId);

	/**
	 * 
	 * @param userId
	 * @param queryDto
	 * @return
	 */
	List<Map<String, Object>> filterAllDocumentsTab(Long userId, TaskQueryDTO queryDto);

	/**
	 * @param userId
	 * @param draftDataDTO
	 * @return
	 */
	Map<String, Object> saveAsDraft(Long userId, DraftDataDTO draftDataDTO); 

	/**
	 * @param userId
	 * @param applicationId
	 * @return
	 */
	Map<String, Object> getSaveAsDraft(Long draftId);

	String deleteDraftedData(String applicationNo);

	List<DraftDataDTO> getDraftByAppId(Long applicationId);

	/**
	 * @param applicationInstanceId
	 * @return
	 */
	boolean processTranslation(Long applicationInstanceId);

	/**
	 * @param userIdFromAuthentication
	 * @param houseIdFromAuthentication
	 * @return
	 */
	InProgressCountResponse getInProgressCount(Long userIdFromAuthentication, Long houseIdFromAuthentication);

	TaskResponseDTO getDocStatusForDate (long applicationId, LocalDate docDate);
}
