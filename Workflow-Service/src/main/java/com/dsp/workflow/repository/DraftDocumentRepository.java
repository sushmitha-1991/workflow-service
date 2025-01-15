/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dsp.workflow.entity.DraftDocument;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.transaction.Transactional;

@Repository
public interface DraftDocumentRepository extends JpaRepository<DraftDocument, Long> {

	/**
	 * @param userId
	 * @param applicationId
	 * @return
	 */
	@Query("SELECT d.draftData FROM DraftDocument d WHERE d.isActive = true AND d.isDeleted = false AND d.createdBy = :userId AND d.applicationId = :applicationId")
	List<JsonNode> findDraftDataByUserAndApplication(@Param("userId") Long userId,
			@Param("applicationId") Long applicationId);

	/**
	 * @param applicationId
	 * @param userId
	 * @param userName
	 */
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO workflow.workflow_history (sub_module_id, pending_with, pending_with_name, draft_id, status) VALUES (:applicationId, :userId, :userName, :draftId, 'Draft')", nativeQuery = true)
	void saveIntoWorkflowHistory(Long applicationId, Long userId, String userName, Long draftId);

}
