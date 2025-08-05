package com.dsp.workflow.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMetadataDTO {

	private Long id;
	private String documentId;
	private Integer documentMasterId;
	private Long fileSize;
	private String mimeType;
	private UUID uuid;
	private Integer version;
	private String filePath;
	private Long refId;
	private String refType;
	private String applicationNo;
}
