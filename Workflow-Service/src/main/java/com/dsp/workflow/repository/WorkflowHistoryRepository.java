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
	@Transactional
	@Query(value = "UPDATE workflow.workflow_history SET action_taken_by = :senderUserId, action_taken_by_name = :userName, "
			+ "status = :status, status_key = :statusKey, action_taken_on = :actionTakenOn, current_task_id = :taskId "
			+ "WHERE process_instance_id = :processInstanceId AND application_instance_id = :applicationInstanceId "
			+ "AND action_taken_by IS NULL", nativeQuery = true)
	void updateWorkflowHistoryForStepType(@Param("processInstanceId") String processInstanceId,
			@Param("applicationInstanceId") Long applicationInstanceId, @Param("senderUserId") Long senderUserId,
			@Param("userName") String userName, @Param("actionTakenOn") LocalDateTime actionTakenOn,
			@Param("status") String status, @Param("statusKey") String statusKey, @Param("taskId") String taskId);

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

	@Query(value = """
			    SELECT applicationNo, documentName, "date", LSNo, SSNo, RSSNo, status, pendingAt, refId,
			           applicationId, applicationinstanceId, taskName, taskId, actionPerformed, formUrl, actionUrl, ma.translate_flag, ma.download_flag
			    FROM (
			        SELECT DISTINCT ON (ai.application_no)
			               ai.application_no AS applicationNo,
			               a.name AS documentName,
			               ai.document_date AS date,
			               ai.lok_sabha_session AS LSNo,
			               ai.lok_sabha_session_number AS SSNo,
			               ai.rajya_sabha_session AS RSSNo,
			               ai.current_status AS status,
			               CASE
			                   WHEN ai.current_status = 'Approved' THEN NULL
			                   ELSE CONCAT(wh.pending_with_name)
			               END AS pendingAt,
			               ai.ref_id AS refId,
			               ai.application_id AS applicationId,
			               ai.id AS applicationinstanceId,
			               'IN-PROCESS' AS taskName,
			               wh.next_task_id AS taskId,
			               'True' AS actionPerformed,
			               a.form_url AS formUrl,
			               wh.action_url AS actionUrl
			        FROM workflow.workflow_history wh
			        JOIN master.application_instance ai ON wh.application_instance_id = ai.id
			        JOIN master.applications a ON ai.application_id = a.id
			        JOIN authentication.base_user u ON u.id = wh.pending_with
			        JOIN authentication.user_roles ur ON ur.user_id = u.id AND ur.is_active = true
			        JOIN authentication.roles r ON r.id = ur.role_id
			        JOIN master.offices o ON o.id = ur.office_id
			        WHERE wh.pending_with = :userId
			          AND wh.action_taken_by IS NULL

			        UNION

			        SELECT DISTINCT ON (ai.application_no)
			               ai.application_no AS applicationNo,
			               a.name AS documentName,
			               ai.document_date AS date,
			               ai.lok_sabha_session AS LSNo,
			               ai.lok_sabha_session_number AS SSNo,
			               ai.rajya_sabha_session AS RSSNo,
			               CASE
			                   WHEN ai.current_status = 'Returned' THEN 'In Progress'
			                   ELSE ai.current_status
			               END AS status,
			               CASE
			                   WHEN ai.current_status = 'Approved' THEN NULL
			                   ELSE CONCAT(wh.pending_with_name)
			               END AS pendingAt,
			               ai.ref_id AS refId,
			               ai.application_id AS applicationId,
			               ai.id AS applicationinstanceId,
			               'IN-PROCESS' AS taskName,
			               wh.next_task_id AS taskId,
			               'False' AS actionPerformed,
			               a.form_url AS formUrl,
			               wh.action_url AS actionUrl
			        FROM workflow.workflow_history wh
			        JOIN master.application_instance ai ON wh.application_instance_id = ai.id
			        JOIN master.applications a ON ai.application_id = a.id
			        JOIN authentication.base_user u ON u.id = wh.pending_with
			        JOIN authentication.user_roles ur ON ur.user_id = u.id AND ur.is_active = true
			        JOIN authentication.roles r ON r.id = ur.role_id
			        JOIN master.offices o ON o.id = ur.office_id
			        WHERE wh.id IN (
			            SELECT MAX(wh2.id)
			            FROM workflow.workflow_history wh2
			            WHERE wh2.application_instance_id IN (
			                SELECT wh3.application_instance_id
			                FROM workflow.workflow_history wh3
			                WHERE wh3.pending_with IS NOT NULL
			                  AND wh3.action_taken_by = :userId
			                  AND wh3.application_instance_id NOT IN (
			                      SELECT wh1.application_instance_id
			                      FROM workflow.workflow_history wh1
			                      WHERE wh1.action_taken_by IS NULL
			                        AND wh1.pending_with = :userId
			                  )
			            )
			            GROUP BY wh2.application_instance_id
			        )

			        UNION

			        SELECT DISTINCT ON (ii.application_no)
			               ii.application_no AS applicationNo,
			               a.name AS documentName,
			               ii.document_date AS date,
			               ii.lok_sabha_session AS LSNo,
			               ii.lok_sabha_session_number AS SSNo,
			               ii.rajya_sabha_session AS RSSNo,
			               ii.current_status AS status,
			               ii.pending_at AS pendingAt,
			               ii.ref_id AS refId,
			               ii.application_id AS applicationId,
			               ii.id AS applicationinstanceId,
			               'IN-PROCESS' AS taskName,
			               NULL AS taskId,
			               'False' AS actionPerformed,
			               a.form_url AS formUrl,
			               NULL AS actionUrl
			        FROM master.input_instance ii
			        JOIN master.applications a ON a.id = ii.application_id
			        JOIN authentication.base_user u ON u.id = ii.created_by
			        WHERE ii.created_by = :userId
			    ) A
			    LEFT JOIN master.applications ma on ma.id = A.applicationId
			    ORDER BY "date" DESC
			""", nativeQuery = true)
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

	/**
	 * @param processInstanceId
	 * @param applicationInstanceId
	 * @param senderUserId
	 * @param userName
	 * @param actionTakenOn
	 * @param status
	 * @param statusKey
	 * @param taskId
	 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE workflow.workflow_history SET action_taken_by = :senderUserId, action_taken_by_name = :userName, "
			+ "status = :status, status_key = :statusKey, action_taken_on = :actionTakenOn, current_task_id = :taskId, pending_with = NULL, pending_with_name = NULL "
			+ "WHERE process_instance_id = :processInstanceId AND application_instance_id = :applicationInstanceId "
			+ "AND action_taken_by IS NULL", nativeQuery = true)
	void updateWorkflowHistoryForFinalStepType(@Param("processInstanceId") String processInstanceId,
			@Param("applicationInstanceId") Long applicationInstanceId, @Param("senderUserId") Long senderUserId,
			@Param("userName") String userName, @Param("actionTakenOn") LocalDateTime actionTakenOn,
			@Param("status") String status, @Param("statusKey") String statusKey, @Param("taskId") String taskId);

	/**
	 * @param lokSabhaSession
	 * @return
	 */
	@Query(value = "SELECT lok_sabha_number from master.lok_sabha_masters where id = :lokSabhaSession", nativeQuery = true)
	String getLokSabhaSession(Long lokSabhaSession);

	/**
	 * @param lokSabhaSessionNumber
	 * @return
	 */
	@Query(value = "SELECT session_number from master.lok_sabha_session_masters where id = :lokSabhaSessionNumber", nativeQuery = true)
	String getLokSabhaSessionNumber(Long lokSabhaSessionNumber);

	/**
	 * @param rajyaSabhaSession
	 * @return
	 */
	@Query(value = "SELECT session_number from master.rajya_sabha_session_master where id = :rajyaSabhaSession", nativeQuery = true)
	String getRajyaSabhaSession(Long rajyaSabhaSession);


	@Modifying
    @Transactional
    @Query(value = "DELETE from workflow.workflow_history where draft_id = :draftId", nativeQuery=true)    
    void deleteByDraftId(Long draftId);


}
