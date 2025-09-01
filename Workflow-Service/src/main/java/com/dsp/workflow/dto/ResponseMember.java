package com.dsp.workflow.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMember {
    private Long id;
    private String memberCode;
    private String photoUrl;
    private UUID photoUuid;
    private String signUrl;
    private UUID signUuid;
    private String house;

}
