/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.mapper;

import java.time.LocalDate;
import java.util.UUID;

import com.dsp.workflow.config.XSSConfig.AllowHtml;
import com.dsp.workflow.config.XSSConfig.NoEscape;

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
	@NoEscape
	String formUrl;
	@NoEscape
	String actionUrl;
	Boolean translateFlag;
	Boolean downloadFlag;
	Long draftId;
	@NoEscape
	String publishedUrl;
	UUID documentUUIDEn;
	UUID documentUUIDHi;
	@AllowHtml
	String htmlContentEn;
	@AllowHtml
	String htmlContentHi;
	String editorId;
	@NoEscape
    String saveAsDraftUrl;
        UUID uploadedDocumentUUID;
}