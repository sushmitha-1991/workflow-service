/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.notesheet.dto;

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
	private String historyId;
	private int applicationInstanceId;
	private String actionTaken;
}
