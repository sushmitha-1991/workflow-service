package com.dsp.workflow.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dsp.workflow.entity.PublishDocumentTemp;
import com.dsp.workflow.entity.PublishHistoryEntity;

public interface PublishDocumentTempRepo extends JpaRepository<PublishDocumentTemp, Long>, JpaSpecificationExecutor<PublishDocumentTemp> {
    

}
