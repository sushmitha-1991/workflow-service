package com.dsp.workflow.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.dsp.workflow.config.XSSConfig.AllowHtml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublishHistoryDTO {

    private Long id;
    private String lokSabhaNo;
    private String lokSabhaSessionNo;
    private String rajyaSabhaSessionNo;
    
    private Long applicationId;
    private String applicationName;
    private String applicationNo;
    
    private UUID publishUuid;
    @AllowHtml
    private String publishUrl;
    private String fileType;
    private String locale;

    private LocalDate forDate;
    private Boolean publishStatus;
    
    private String publishByUser;
    private LocalDate publishDate;
    
    private String unpublishByUser;
    private LocalDate unpublishDate;
    
    private Long officeId;
    private Integer year;
	private Integer month;
    private String fileTypeName;
    private String title;
    private String house;

    private String branch; // maps to primary_office_name

}
