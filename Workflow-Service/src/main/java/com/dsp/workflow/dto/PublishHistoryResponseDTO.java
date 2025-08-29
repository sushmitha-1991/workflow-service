package com.dsp.workflow.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublishHistoryResponseDTO {
	private Long id;
	private String lokSabhaNo;
	private String lokSabhaSessionNo;
	private String rajyaSabhaSessionNo;
	private LocalDate publishDate;
	private String publishByUser;
	private UUID publishUuid;
	private Boolean publishStatus;
	private LocalDate unpublishDate;
	private String unpublishByUser;
	private Long officeId;
	private Long applicationId;
	private String branch;
	private String applicationName;

}
