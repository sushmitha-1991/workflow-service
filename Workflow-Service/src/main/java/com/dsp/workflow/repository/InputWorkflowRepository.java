package com.dsp.workflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dsp.workflow.entity.InputWorkflowHistory;
import com.dsp.workflow.entity.WorkflowHistory;

/**
 * 
 */
@Repository

public interface InputWorkflowRepository extends JpaRepository<InputWorkflowHistory, Long>{

	List<InputWorkflowHistory> findByInputInstanceId(@Param("instanceId")Long instanceId); 
	
}
