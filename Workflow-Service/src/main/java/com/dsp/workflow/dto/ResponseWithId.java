package com.dsp.workflow.dto;
import com.dsp.workflow.config.XSSConfig.AllowHtml;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
@Builder
public class ResponseWithId {
    Long id;
    String Url;
    @AllowHtml
    String completeUrl;
}
