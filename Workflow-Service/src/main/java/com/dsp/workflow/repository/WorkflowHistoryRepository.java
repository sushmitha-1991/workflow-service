/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dsp.workflow.entity.WorkflowHistory;

import jakarta.transaction.Transactional;

/**
 * Repository interface for WorkflowHistory entity.
 * 
 * Provides CRUD operations and any custom query methods.
 */
@Repository
public interface WorkflowHistoryRepository extends JpaRepository<WorkflowHistory, Long> {

	@Modifying
	@Transactional // Ensure this is present to manage transactions for the update query
	@Query(value = "UPDATE workflow.workflow_history SET action_taken_by = :senderUserId, action_taken_by_name = :userName, "
			+ "status = :status, status_key = :statusKey, action_taken_on = :actionTakenOn "
			+ "WHERE process_instance_id = :processInstanceId AND application_instance_id = :applicationInstanceId "
			+ "AND action_taken IS NULL", nativeQuery = true)
	void updateWorkflowHistoryForStepType(@Param("processInstanceId") String processInstanceId,
			@Param("applicationInstanceId") Long applicationInstanceId, @Param("senderUserId") Long senderUserId,
			@Param("userName") String userName, @Param("actionTakenOn") LocalDateTime actionTakenOn,
			@Param("status") String status, @Param("statusKey") String statusKey);
	
	List<WorkflowHistory> findBySubModuleIdAndActionTakenBy(String subModuleId, Long UserId);

	/**
	 * @param subModuleId
	 * @param userId
	 * @return
	 */
	WorkflowHistory findTopBySubModuleIdAndActionTakenByOrderByCreatedAtDesc(String subModuleId, Long userId);

	/**
	 * @param applicationInstanceId
	 * @return
	 */
	List<WorkflowHistory> findByApplicationInstanceId(Long applicationInstanceId);


}
