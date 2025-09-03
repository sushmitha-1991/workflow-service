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
public class ResponseDocsUuids {
     
    private String url;
    private UUID uuid;
    private String house;
    private String fileType;
}
