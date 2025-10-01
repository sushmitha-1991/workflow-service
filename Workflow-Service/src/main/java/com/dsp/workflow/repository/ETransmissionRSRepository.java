package com.dsp.workflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.dsp.workflow.entity.ETransmissionRSUploadTempEntity;

public interface ETransmissionRSRepository extends JpaRepository<ETransmissionRSUploadTempEntity, Long>,JpaSpecificationExecutor<ETransmissionRSUploadTempEntity> {

}

