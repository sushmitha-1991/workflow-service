/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.entity;

import java.time.LocalDate;

import com.dsp.workflow.util.JsonNodeConverter;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "drafted_document", schema = "workflow")
public class DraftDocument extends Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "draft_document_seq")
	@SequenceGenerator(name = "draft_document_seq", sequenceName = "workflow.draft_document_seq", allocationSize = 1)
	private Long id;

	@Column(name = "application_id", nullable = false)
	private Long applicationId;

	@Column(name = "application_no", nullable = false)
	private String applicationNo;

	@Column(name = "lok_sabha_session", nullable = false)
	private String lokSabhaSession;

	@Column(name = "lok_sabha_session_number", nullable = false)
	private String lokSabhaSessionNumber;

	@Column(name = "rajya_sabha_session", nullable = false)
	private String rajyaSabhaSession;

	@Column(name = "draft_data", nullable = false, columnDefinition = "jsonb")
	@Convert(converter = JsonNodeConverter.class) // Use the converter here
	private JsonNode draftData;

	@OneToOne(mappedBy = "draftDocument", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private WorkflowHistory workflowHistory;

	@Column(name = "for_date", nullable = true)
	private LocalDate forDate;
	
	@Column(name = "only_office_dsp2_key", nullable = false)
	private String onlyOfficeDsp2Key;
}
