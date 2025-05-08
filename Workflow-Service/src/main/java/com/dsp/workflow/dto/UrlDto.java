package com.dsp.workflow.dto;

import org.joda.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class UrlDto {
    private String name;
    private String date;
    private String url;
    private String fileType;
    private String fileName;

}
