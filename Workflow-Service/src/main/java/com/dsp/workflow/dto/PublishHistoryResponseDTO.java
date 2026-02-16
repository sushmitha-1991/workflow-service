package com.dsp.workflow.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublishHistoryResponseDTO {
	private Long id;
	private String lokSabhaNo;
	private String lokSabhaSessionNo;
	private String rajyaSabhaSessionNo;
	private LocalDateTime publishDate;
	private String publishByUser;
	private UUID publishUuid;
	private Boolean publishStatus;
	private LocalDateTime unpublishDate;
	private String unpublishByUser;
	private LocalDate documentDate;
	private Long officeId;
	private Long applicationId;
	private String applicationNo;
	private String branch;
	private String applicationName;
	private Long refId;
	private String publishedUrl;
	private Long noticeId;

}
