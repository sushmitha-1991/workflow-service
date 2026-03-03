package com.dsp.workflow.dto;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Setter
@Getter
public class DocumentDTO {
	private Long id;
	private LocalDate date;
	private Long applicationId;
	private String applicationNo;
	private String currentStatus;
	private String note;
	private String footNote;
	private String dsp2Key;
	private String lokSabhaNo;
	private String lokSabhaSessionNo;
	private String rajyaSabhaSessionNo;
}
