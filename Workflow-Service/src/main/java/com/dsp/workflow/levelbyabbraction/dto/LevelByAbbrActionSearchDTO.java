/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.levelbyabbraction.dto;

import lombok.Data;

/**
 * 
 */
@Data
public class LevelByAbbrActionSearchDTO {
	private String level;
	private Long applicationId;
	private Integer[] roleId;

}
