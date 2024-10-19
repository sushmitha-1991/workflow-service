package com.dsp.workflow.dto;

import java.util.List;
import java.util.Map;

import com.dsp.workflow.notesheet.dto.NotesheetDTO;
import com.dsp.workflow.util.SendToTypeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CompleteTaskRequestDTO extends NotesheetDTO {

	@NotEmpty
	String taskId;

	String processInstanceId;

	private Map<String, Object> additional_details;

	private Long officeId;
	private String application_no;
	private String stepType;
	private String status;
	private List<ProcessStepwiseDocumentsDTO> document;

	/**
	 * A static key representing the action to be sent to the reviewer. Key:
	 * sendToReviewer
	 */
	@JsonDeserialize(using = SendToTypeDeserializer.class)
	private SendToType sendTo;

	public enum SendToType {
		REVIEWER("Reviewer"), PUBLISH("Publish"), APPROVER("Approver"), CREATOR("Creator");

		private final String label;

		SendToType(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}
	}

}
