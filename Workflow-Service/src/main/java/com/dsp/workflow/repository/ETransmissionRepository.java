package com.dsp.workflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.dsp.workflow.entity.ETransmissionUploadTempEntity;
import com.dsp.workflow.util.ETransmissionDataUpload;

public interface ETransmissionRepository extends JpaRepository<ETransmissionUploadTempEntity, Long>,
 JpaSpecificationExecutor<ETransmissionUploadTempEntity> {

}

