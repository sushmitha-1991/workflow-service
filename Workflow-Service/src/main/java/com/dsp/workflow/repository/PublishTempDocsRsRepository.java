package com.dsp.workflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.dsp.workflow.entity.PublishDocumentTemp;
import com.dsp.workflow.entity.PublishDocumentTempRS;

public interface PublishTempDocsRsRepository extends JpaRepository<PublishDocumentTempRS, Long>,
        JpaSpecificationExecutor<PublishDocumentTempRS> {

    List<PublishDocumentTemp> findByApplicationName(String applicationName);

}