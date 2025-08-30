package com.dsp.workflow.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class ResponseMember {
    private Long id;
    private String memberCode;
    private String photoUrl;
    private UUID photoUuid;
    private String signUrl;
    private UUID signUuid;
    private String house;

}
