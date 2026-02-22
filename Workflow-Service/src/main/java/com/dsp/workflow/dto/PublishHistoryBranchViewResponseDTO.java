package com.dsp.workflow.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublishHistoryBranchViewResponseDTO {

	private String lokSabhaNo;
    private String sessionNo;
    private LocalDate forDate;
    private String applicationName;
    private Long applicationId;
    private String applicationNo;
    private String locale;
    private String publishUrl;
    private String downloadUrl;
}
