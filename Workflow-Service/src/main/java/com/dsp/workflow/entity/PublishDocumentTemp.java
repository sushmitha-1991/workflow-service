package com.dsp.workflow.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "publish_document_temp", schema = "migration")
public class PublishDocumentTemp {
    @Id
    private Long id;
    private String url;
    @Column(name = "lsno")
    private String lsNo;
    private String sesNo;
    private String completeUrl;
    private UUID publishUuid;
    private Long applicationId;
    private String applicationName;
    private String locale;
    private Boolean isUpdatedUuid;
}