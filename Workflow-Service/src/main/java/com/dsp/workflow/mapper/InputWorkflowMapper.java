/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.mapper;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceDto;

import com.dsp.workflow.dto.TaskHistoryDTO;
import com.dsp.workflow.entity.InputWorkflowHistory;
import com.dsp.workflow.entity.WorkflowHistory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class InputWorkflowMapper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final ZoneId indiaZone = ZoneId.of("Asia/Kolkata");

    public WorkflowHistory mapToWorkflowHistory (InputWorkflowHistory taskInstance) {

        return WorkflowHistory.builder()
                .pendingWithName(taskInstance.getPendingWithName())
                .actionTakenByName(taskInstance.getActionTakenByName())
                .subModuleId(taskInstance.getApplicationId().toString())
                .status(taskInstance.getStatus())
                .note(taskInstance.getCommentForDisplay())
                .applicationInstanceId(taskInstance.getApplicationRecordId())
                .actionTakenOn(taskInstance.getCreatedAt())
                .build();
    }

}
