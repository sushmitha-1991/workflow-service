package com.dsp.workflow.repository;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.dsp.workflow.entity.PublishDocumentTemp;

public interface PublishDocumentTempRepo extends JpaRepository<PublishDocumentTemp, Long>, JpaSpecificationExecutor<PublishDocumentTemp> {

    List<PublishDocumentTemp> findByApplicationName(String applicationName);

    

}
