package com.dsp.workflow.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dsp.workflow.entity.InputWorkflowHistory;

/**
 * 
 */
@Repository
public interface InputWorkflowRepository extends JpaRepository<InputWorkflowHistory, Long>{

	List<InputWorkflowHistory> findByInputInstanceId(@Param("instanceId")Long instanceId); 
	
	InputWorkflowHistory findTop1ByInputInstanceIdOrderByCreatedAtDesc(@Param("instanceId")Long instanceId); 

}
