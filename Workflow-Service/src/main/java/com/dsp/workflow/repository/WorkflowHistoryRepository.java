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

	@Query(value = "SELECT DISTINCT ON (ai.application_no) ai.application_no AS applicationNo, a.name AS documentName, "
			+ "wh.pending_start_date AS date, '18' AS LSNo, 'III' AS SSNo, "
			+ "ai.current_status AS status, CONCAT(wh.pending_with_name, ' - ', r.abbr, ' - ', o.office_name) AS pendingAt, ai.ref_id AS refId, ai.application_id AS applicationId, ai.process_instance_id as processInstanceId, ai.id as applicationinstanceId "
			+ "FROM workflow.workflow_history wh "
			+ "JOIN master.application_instance ai ON wh.application_instance_id = ai.id "
			+ "JOIN master.applications a ON ai.application_id = a.id "
			+ "JOIN authentication.base_user u ON u.id = :userId "
			+ "JOIN authentication.user_roles ur ON ur.user_id = u.id AND ur.is_active = true "
			+ "JOIN authentication.roles r ON r.id = ur.role_id " + "JOIN master.offices o ON o.id = ur.office_id "
			+ "WHERE wh.pending_with = :userId OR wh.action_taken_by = :userId", nativeQuery = true)
	List<Object[]> findAllDocumentsForUser(@Param("userId") Long userId);

	@Query(value = "SELECT DISTINCT ON (ai.application_no) ai.application_no AS applicationNo, "
			+ "a.name AS documentName, " + "wh.action_taken_on AS date, " + "'18' AS LSNo, " + "'III' AS SSNo, "
			+ "ai.current_status AS status, "
			+ "CONCAT(wh.action_taken_by_name, ' - ', r.abbr, ' - ', o.office_name) AS pendingAt, "
			+ "ai.ref_id AS refId, " + "ai.application_id AS applicationId, " + "ai.id AS applicationInstanceId "
			+ "FROM workflow.workflow_history wh "
			+ "JOIN master.application_instance ai ON wh.application_instance_id = ai.id "
			+ "JOIN master.applications a ON ai.application_id = a.id "
			+ "JOIN authentication.base_user u ON u.id = :userId "
			+ "JOIN authentication.user_roles ur ON ur.user_id = u.id AND ur.is_active = true "
			+ "JOIN authentication.roles r ON r.id = ur.role_id " + "JOIN master.offices o ON o.id = ur.office_id "
			+ "WHERE wh.action_taken_by = :userId AND ai.current_status = 'Approved' "
			+ "ORDER BY ai.application_no, wh.action_taken_on DESC", nativeQuery = true)
	List<Object[]> findAllApprovedDocumentsForUser(@Param("userId") Long userId);
}
