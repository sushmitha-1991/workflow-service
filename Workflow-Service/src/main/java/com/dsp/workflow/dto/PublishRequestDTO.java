/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublishRequestDTO {

    private String taskId;

    private String note;

    private Integer senderRoleId;

    private Integer applicationInstanceId;

    private String actionTaken;

    // Getters and Setters
}

