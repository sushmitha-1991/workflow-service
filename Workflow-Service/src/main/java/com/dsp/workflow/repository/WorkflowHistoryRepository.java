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

import com.dsp.workflow.dto.InProgressCountResponse;
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
			+ "status = :status, status_key = :statusKey, action_taken_on = :actionTakenOn, current_task_id = :taskId, pending_with = :receiverUserId, pending_with_name = :receiverUserName, pending_start_date = :actionTakenOn "
			+ "WHERE process_instance_id = :processInstanceId AND application_instance_id = :applicationInstanceId "
			+ "AND action_taken_by IS NULL", nativeQuery = true)
	void updateWorkflowHistoryForStepType(@Param("processInstanceId") String processInstanceId,
			@Param("applicationInstanceId") Long applicationInstanceId, @Param("senderUserId") Long senderUserId,
			@Param("userName") String userName, @Param("actionTakenOn") LocalDateTime actionTakenOn,
			@Param("status") String status, @Param("statusKey") String statusKey, @Param("taskId") String taskId,
			@Param("receiverUserId") Long receiverUserId, @Param("receiverUserName") String receiverUserName);

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

	/**
	 * @param applicationInstanceId
	 * @return
	 */
	List<WorkflowHistory> findByApplicationInstanceIdOrderByCreatedAtDesc(Long applicationInstanceId);

	@Modifying
	@Transactional
	@Query(value = "DELETE from workflow.workflow_history where draft_id = :draftId", nativeQuery = true)
	void deleteByDraftId(Long draftId);

	/**
	 * @param application_no
	 * @return
	 */
	@Query(value = "select abbr from master.applications where id = :application_no", nativeQuery = true)
	String getApplicationAbbr(Long application_no);

	/**
	 * @param userId
	 * @param houseId
	 * @return
	 */
	@Query(value = """
			SELECT status AS status, COUNT(status) AS count
			FROM (
			    -- START: Big SQL Union Query
			    select distinct on (ai.application_no) ai.application_no as applicationNo,
			           ai.current_status as status
			    from workflow.workflow_history wh
			    join master.application_instance ai on wh.application_instance_id = ai.id
			    join master.applications a on ai.application_id = a.id
			    where wh.pending_with = :userId
			      and wh.action_taken_by is null
			      and (ai.current_status != 'Approved' and ai.current_status != 'Published')
			      and a.id not in (
			          select a.id
			          from master.applications a, master.modules m
			          where a.module_id = m.id and a.abbr in ('SYN') and m.house_id = :houseId
			      )

			    union

			    select distinct on (ai.application_no) ai.application_no as applicationNo,
			           case when ai.current_status = 'Returned' then 'In Progress' else ai.current_status end as status
			    from (
			        select wh1.*
			        from workflow.workflow_history wh1
			        where wh1.id in (
			            select max(id)
			            from workflow.workflow_history wh2
			            where wh2.application_instance_id in (
			                select application_instance_id
			                from workflow.workflow_history wh3
			                where wh3.action_taken_by = :userId
			            )
			            group by application_instance_id
			        )
			        and (wh1.pending_with != :userId or wh1.pending_with is null)
			    ) wh
			    join master.application_instance ai on wh.application_instance_id = ai.id
			    join master.applications a on ai.application_id = a.id
			    where (ai.current_status != 'Approved' and ai.current_status != 'Published')
			      and a.id not in (
			          select a.id
			          from master.applications a, master.modules m
			          where a.module_id = m.id and a.abbr in ('SYN') and m.house_id = :houseId
			      )

			    union

			    select distinct on (ii.application_no) ii.application_no as applicationNo,
			           case
			               when ii.application_instance_id is not null and applicationInstance.id = ii.application_instance_id
			                    and applicationInstance.current_status not in ('Returned', 'Draft')
			                   then applicationInstance.current_status
			               else ii.current_status
			           end as status
			    from master.input_instance ii
			    join master.applications a on a.id = ii.application_id
			    join authentication.base_user u on u.id = ii.created_by
			    left join master.application_instance applicationInstance on applicationInstance.id = ii.application_instance_id
			    where ii.created_by = :userId

			    union

			    select distinct on (dd.application_no) dd.application_no as applicationNo,
			           wh.status as status
			    from workflow.workflow_history wh
			    left join workflow.drafted_document dd on dd.id = wh.draft_id
			    left join master.applications a on a.id = dd.application_id
			    left join authentication.base_user u on u.id = wh.pending_with
			    where wh.pending_with = :userId
			      and wh.draft_id IS NOT NULL

			    union

			    select distinct on (ai.application_no) ai.application_no as applicationNo,
			           ai.current_status as status
			    from workflow.workflow_history wh
			    join master.application_instance ai on wh.application_instance_id = ai.id
			    join master.applications a on ai.application_id = a.id
			    where (ai.current_status = 'Approved' or ai.current_status = 'Published')
			      and a.primary_office_id in (
			          select office_id
			          from authentication.user_roles
			          where user_id = :userId and is_active = true and is_deleted = false
			      )
			      and wh.is_active = true and wh.is_deleted = false

			    union

			    SELECT r.ref_no as applicationNo, 'In Progress' as status
			    FROM synopsis.roster_detail rd
			    JOIN synopsis.roster r ON rd.roster_id = r.id
			    left join master.lok_sabha_masters lsm on lsm.id = r.lok_sabha_no
			    left join master.lok_sabha_session_masters lssm on lssm.id = r.lok_sabha_session_no
			    left join master.rajya_sabha_session_master rssm on rssm.id = r.rajya_sabha_session_no
			    left join authentication.base_user bu on bu.id = rd.editor_id
			    left join master.designations d on bu.designation_id = d.id
			    left join authentication.user_roles ur on bu.id = ur.user_id
			    LEFT JOIN authentication.roles ar ON ur.role_id = ar.id
			    LEFT JOIN master.offices o ON o.id = ur.office_id
			    WHERE rd.editor_id = :userId
			      AND r.roster_date IS NOT NULL
			      AND r.roster_type = 'Daily'
			      AND rd.task_status <> 'Completed'
			    GROUP BY r.ref_no, r.roster_date, rd.editor_id, lsm.lok_sabha_number,
			             lssm.session_number, rssm.session_number, rd.supplementary_flag,
			             bu.first_name, bu.last_name, d.abbr, o.office_name

			    union

			    SELECT ah.ref_no as applicationNo, 'In Progress' as status
			    FROM translation.translation_input inp
			    JOIN translation.allocation_history ah ON inp.id = ah.translation_input_id
			    left join master.applications ma on ma.id = inp.document_type_id
			    left join master.lok_sabha_session_masters lssm on lssm.id = inp.lok_sabha_session_no
			    left join master.lok_sabha_masters lsm on lsm.id = inp.lok_sabha_no
			    left join master.rajya_sabha_session_master rssm on rssm.id = inp.rajya_sabha_session_no
			    left join authentication.base_user bu on bu.id = ah.sent_to_user_id
			    left join master.designations d on bu.designation_id = d.id
			    left join authentication.user_roles ur on bu.id = ur.user_id
			    LEFT JOIN authentication.roles ar ON ur.role_id = ar.id
			    LEFT JOIN master.offices o ON o.id = ur.office_id
			    WHERE ah.sent_to_user_id = :userId
			      AND inp.document_date IS NOT NULL
			      AND ah.is_active = true
			      AND ah.is_deleted = false

			    union

			    SELECT da.ref_no as applicationNo, 'In Progress' as status
			    FROM corrected_debate.allocation da
			    left join master.lok_sabha_masters lsm on lsm.id = da.lok_sabha_no
			    left join master.lok_sabha_session_masters lssm on lssm.id = da.lok_sabha_session_no
			    left join master.rajya_sabha_session_master rssm on rssm.id = da.rajya_sabha_session_no
			    left join authentication.base_user bu on bu.id = da.allocateto
			    WHERE da.allocateto = :userId
			      and da.status <> 'Completed'

			    union

			    SELECT da.ref_no as applicationNo, 'In Progress' as status
			    FROM corrected_debate.allocation_history da
			    left join corrected_debate.translation_input i on i.id = da.translation_input_id
			    left join master.lok_sabha_masters lsm on lsm.id = i.lok_sabha_no
			    left join master.lok_sabha_session_masters lssm on lssm.id = i.lok_sabha_session_no
			    left join master.rajya_sabha_session_master rssm on rssm.id = i.rajya_sabha_session_no
			    left join authentication.base_user bu on bu.id = da.sent_to_user_id
			    WHERE da.sent_to_user_id = :userId
			      and da.status <> 'Completed'

			    union

			    SELECT es.application_no as applicationNo, 'Published' as status
			    FROM agenda_papers.e_summon es
			    WHERE es.status = 'Published'
			      and es.house_id = :houseId
			      and es.is_active = true
			      and es.is_deleted = false
			      and exists (
			          select 1
			          from authentication.user_roles ur
			          where ur.user_id = :userId
			            and ur.office_id in (
			                select a.primary_office_id
			                from master.applications a
			                where a.abbr = 'ES'
			            )
			      )

			    union

			    select distinct on (ai.document_date, ai.application_id, rd.document_type) ai.application_no as applicationNo,
			           ai.current_status as status
			    from workflow.workflow_history wh
			    join master.application_instance ai on wh.application_instance_id = ai.id
			    join master.applications a on ai.application_id = a.id
			    join synopsis.synopsis s on s.id = ai.id
			    join synopsis.roster_detail rd on rd.id = s.roster_detail_id
			    where wh.pending_with = :userId
			      and wh.action_taken_by is null
			      and (ai.current_status != 'Approved' and ai.current_status != 'Published')
			      and a.id in (
			          select a.id
			          from master.applications a, master.modules m
			          where a.module_id = m.id and a.abbr in ('SYN')
			      )
			    -- END: Union
			) A
			WHERE status = 'In Progress'
			GROUP BY status
			""", nativeQuery = true)
	InProgressCountResponse getInProgressStatusCount(@Param("userId") Long userId, @Param("houseId") Long houseId);

}
