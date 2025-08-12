/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.entity;

import com.dsp.workflow.notesheet.entity.Notesheet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Builder
@Table(schema = "workflow", name = "note_stepwise_documents")
public class NoteStepwiseDocuments extends Auditable {

	@Id
	@SequenceGenerator(name = "process_stepwise_documents_seq", sequenceName = "workflow.process_stepwise_documents_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "process_stepwise_documents_seq")
	private Long id;

	@Column(name = "document_name", nullable = true)
	private String documentName;

	@Column(name = "document_uuid", nullable = true)
	private String documentUuid;

	@Column(name = "uploaded_by", nullable = true)
	private String uploadedBy;

	@Column(name = "version", nullable = true)
	private String version;

	// Many-to-One relationship with Notesheet
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "notesheet_id", referencedColumnName = "id", nullable = false)
	@ToString.Exclude
	private Notesheet notesheet; // Add this line to establish the relationship

}
