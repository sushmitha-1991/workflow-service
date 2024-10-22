package com.dsp.workflow.entity;

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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(schema = "workflow", name = "application_forms")
public class ApplicationForms extends Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "application_forms_seq_gen")
	@SequenceGenerator(name = "application_forms_seq_gen", sequenceName = "application_forms_seq", allocationSize = 1)
	private Long id;

	@Column(nullable = false, length = 255)
	private String name;

	@Column(length = 500)
	private String description;

	@Column(name = "form_url", nullable = false, length = 255)
	private String formUrl;

	@Column(name = "report_url", nullable = false, length = 255)
	private String reportUrl;

	@Column(name = "display_sequence")
	private int displaySequence;

	@Column(nullable = false, length = 10)
	private String abbr;
}
