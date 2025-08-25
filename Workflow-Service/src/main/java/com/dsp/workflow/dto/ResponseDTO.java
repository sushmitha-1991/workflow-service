package com.dsp.workflow.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO {
    private String applicationName;
    private String url;
    private Long appId;
    private String lsNo;
    private String sessNo;
    private String text;
    private LocalDate docDate;
    private String refNo;
}
