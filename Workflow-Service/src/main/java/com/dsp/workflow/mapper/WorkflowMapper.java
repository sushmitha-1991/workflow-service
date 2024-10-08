/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.mapper;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceDto;

import com.dsp.workflow.dto.TaskHistoryDTO;

public class WorkflowMapper {

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	private static final ZoneId indiaZone = ZoneId.of("Asia/Kolkata");

	public TaskHistoryDTO mapToTaskHistoryDTO(HistoricTaskInstance taskInstance) {
		Date startTimeUtc = taskInstance.getStartTime();
		Date endTimeUtc = taskInstance.getEndTime();

		String startTimeIst = (startTimeUtc != null) ? startTimeUtc.toInstant().atZone(indiaZone).format(formatter)
				: null;
		String endTimeIst = (endTimeUtc != null) ? endTimeUtc.toInstant().atZone(indiaZone).format(formatter) : null;

		return TaskHistoryDTO.builder().taskId(taskInstance.getId()).historyId(taskInstance.getId())
				.taskName(taskInstance.getName()).assignee(taskInstance.getAssignee()).startTime(startTimeIst)
				.endTime(endTimeIst).build();
	}

	// New method for HistoricTaskInstanceDto
	public TaskHistoryDTO mapToTaskHistoryDTO(HistoricTaskInstanceDto taskInstanceDto) {
		String startTimeIst = (taskInstanceDto.getStartTime() != null)
				? taskInstanceDto.getStartTime().toInstant().atZone(indiaZone).format(formatter)
				: null;
		String endTimeIst = (taskInstanceDto.getEndTime() != null)
				? taskInstanceDto.getEndTime().toInstant().atZone(indiaZone).format(formatter)
				: null;

		return TaskHistoryDTO.builder().taskId(taskInstanceDto.getId()).historyId(taskInstanceDto.getId())
				.taskName(taskInstanceDto.getName()).assignee(taskInstanceDto.getAssignee()).startTime(startTimeIst)
				.endTime(endTimeIst).build();
	}

}
