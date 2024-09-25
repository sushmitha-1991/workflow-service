/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.notesheet.dto;

import java.util.Map;

import com.dsp.workflow.notesheet.entity.Notesheet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotesheetDTO {

    private String note;
    private int receiverRoleId;
    private int receiverUserId;
    private int senderRoleId;
    private String applicationhistoryId;
    private int applicationInstanceId;
    private String actionTaken;
    // private Map<String, Object> additionalDetails;

    // Constructor to create DTO from Entity
    public NotesheetDTO(Notesheet notesheet) {
        this.note = notesheet.getNote();
        this.receiverRoleId = notesheet.getReceiverRoleId();
        this.receiverUserId = notesheet.getReceiverUserId();
        this.senderRoleId = notesheet.getSenderRoleId();
        this.applicationhistoryId = notesheet.getApplicationHistoryId();
        this.applicationInstanceId = notesheet.getApplicationInstanceId();
        this.actionTaken = notesheet.getActionTaken();
        //this.additionalDetails = notesheet.getAdditionalDetails();
    }
}
