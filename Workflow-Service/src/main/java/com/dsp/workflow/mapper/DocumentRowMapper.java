/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.mapper;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentRowMapper {

	String applicationNo;
	String documentName;
	LocalDate date;
	String LSNo;
	String SSNo;
	String RSSNo;
	String status;
	String pendingAt;
	String refId;
	String applicationId;
	String applicationInstanceId;
	String taskName;
	String taskId;
	String actionPerformed;
	String formUrl;
	String actionUrl;
	Boolean translateFlag;
	Boolean downloadFlag;
	Long draftId;
	String publishedUrl;
	UUID documentUUIDEn;
	UUID documentUUIDHi;
	String htmlContentEn;
	String htmlContentHi;
	String editorId;
        String saveAsDraftUrl;
        UUID uploadedDocumentUUID;
}