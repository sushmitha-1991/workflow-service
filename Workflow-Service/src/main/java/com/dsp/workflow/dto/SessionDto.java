package com.dsp.workflow.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Setter
@Getter
@AllArgsConstructor
public class SessionDto {
    private String lsNo;
    private String sesNo;
    private String rsNo;
    private String locale;
    private LocalDate fromDate;
    private LocalDate toDate;

}
