package com.dsp.workflow.levelbyabbraction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelByAbbrActionDTO {

    private Long id;
    private Long roleId;
    private String level;
    private String actionKey;
    private String abbr;
    private String stepType;
    private String status;
    private String statusKey;
    private Long applicationId;


}
