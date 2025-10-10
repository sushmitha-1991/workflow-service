package com.dsp.workflow.repository;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.dsp.workflow.entity.MemberPhotoSignTempRS;

public interface MemberTempUploadRepositoryRS extends JpaRepository<MemberPhotoSignTempRS, Long>, JpaSpecificationExecutor<MemberPhotoSignTempRS> {

    // List<MemberPhotoSignTemp> findByApplicationName(String applicationName);

    

}
