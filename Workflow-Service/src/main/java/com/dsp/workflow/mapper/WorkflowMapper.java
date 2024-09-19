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
				.endTime(endTimeIst).durationInMillis(taskInstance.getDurationInMillis()).build();
	}
}
