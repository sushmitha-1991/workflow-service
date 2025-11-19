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
@Table(name = "publish_document_temp_rs_new", schema = "migration")
public class PublishDocumentTempRS {

    @Id
    private Long id;
@Column(name = "lsno")
    private String lsNo;

    private String sesNo;

    private String forDate; // if this is actually a date, better use LocalDate

    private String url;

    private String completeUrl;

    private String applicationName;

    private Long applicationId;

    private String tableName;

    private Integer recordCount;


    private String publishUrl;

    private UUID publishUuid;

    private String locale;

    private Boolean isUpdatedUuid;

}

