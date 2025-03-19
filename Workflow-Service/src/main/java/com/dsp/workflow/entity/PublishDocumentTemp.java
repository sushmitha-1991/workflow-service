package com.dsp.workflow.entity;

import java.util.UUID;

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
    private String completeUrl;
    private UUID publishUuid;
    private Long applicationId;
    public PublishDocumentTemp saveTemp(PublishDocumentTemp publishDocumentTemp) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveTemp'");
    }
}