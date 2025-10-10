/**
 *
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.entity;

import java.time.LocalDate;
import java.util.UUID;

import com.dsp.workflow.config.XSSConfig.AllowHtml;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ApplicationInstance", schema = "master")
public class ApplicationInstance extends Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "application_instance_seq")
	@SequenceGenerator(name = "application_instance_seq", sequenceName = "master.application_instance_seq", allocationSize = 1)
	private Long id;

	@Column(name = "application_id", nullable = false)
	private Long applicationId;

	@Column(name = "applicationNo", nullable = false, length = 255)
	private String applicationNo;

	@Column(name = "ref_Id")
	private Long refId;

	@Column(name = "currentStatus", nullable = false, length = 100)
	private String currentStatus;

	@Column(name = "published_documentUrl", length = 255)
	private String publishedDocumentUrl;

	@Column(name = "process_Instance_Id", length = 255)
	private String processInstanceId;

	@Column(name = "task_id", length = 255)
	private String taskId;

	@Column(name = "is_published")
	private Boolean isPublished;

//	 @Type(type = "jsonb")
//	    @Column(name = "additional_Details", columnDefinition = "jsonb")
//	    private String additionalDetails;

	@AllowHtml
	@Column(name = "html_content_en", columnDefinition = "text")
	private String htmlContentEnglish;
	@AllowHtml
	@Column(name = "html_content_hi", columnDefinition = "text")
	private String htmlContentHindi;

	@Column(name = "lok_sabha_session", length = 30)
	private String lokSabhaSession;

	@Column(name = "lok_sabha_session_number", length = 30)
	private String lokSabhaSessionNumber;

	@Column(name = "other_info", columnDefinition = "jsonb")
	private String otherInfo; // You can use JsonNode if you want structured JSON

	@Column(name = "rajya_sabha_session", length = 30)
	private String rajyaSabhaSession;

	@Column(name = "additional_details", columnDefinition = "text")
	private String additionalDetails;

	@Column(name = "draft")
	private Boolean draft = true;

	@Column(name = "document_date")
	private LocalDate documentDate;

	@Column(name = "published_document_uuid_en")
	private UUID publishedDocumentUuidEn;

	@Column(name = "published_document_uuid_hi")
	private UUID publishedDocumentUuidHi;

	@Column(name = "parent_instance_id")
	private Long parentInstanceId;

	@Column(name = "uploaded_document_uuid")
	private UUID uploadedDocumentUuid;

	@Column(name = "starting_page_no")
	private Long startingPageNo;

	@Column(name = "translation_document_type_id")
	private Long translationDocumentTypeId;

	@Column(name = "only_office_dsp2_key", length = 255)
	private String onlyOfficeDsp2Key;

	@Column(name = "document_display_name", length = 255)
	private String documentDisplayName;

	@Column(name = "ending_page_no_en")
	private Long endingPageNoEn;

	@Column(name = "ending_page_no_hi")
	private Long endingPageNoHi;

	@Column(name = "dsp2_key_hi", length = 255)
	private String dsp2KeyHi;

}
