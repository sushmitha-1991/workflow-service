package com.dsp.workflow.dto;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
@Builder
public class ResponseIdandText {
    Long id;
    String text;
}
