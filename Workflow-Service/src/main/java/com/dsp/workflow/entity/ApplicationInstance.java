/**
 *
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
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
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ApplicationInstance", schema = "master")
public class ApplicationInstance extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "application_Instance_seq")
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

    @Column(name = "html_content_en", columnDefinition = "text")
    private String htmlContentEnglish;
    @Column(name = "html_content_hi", columnDefinition = "text")
    private String htmlContentHindi;

}
