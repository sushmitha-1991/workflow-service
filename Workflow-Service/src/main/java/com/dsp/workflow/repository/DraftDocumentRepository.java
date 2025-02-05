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

import jakarta.transaction.Transactional;

@Repository
public interface DraftDocumentRepository extends JpaRepository<DraftDocument, Long> {

    /**
     * @param userId
     * @param applicationId
     * @return
     */
    @Query("SELECT  d.draftData, d.applicationNo, d.lokSabhaSession, d.lokSabhaSessionNumber, d.rajyaSabhaSession "
            + "FROM DraftDocument d " + "WHERE d.isActive = true AND d.isDeleted = false AND d.id = :draftId")
    List<Object[]> findDraftDataByUserAndApplication(@Param("draftId") Long draftId);

//	/**
//	 * @param applicationId
//	 * @param userId
//	 * @param userName
//	 */
//	@Modifying
//	@Transactional
//	@Query(value = "INSERT INTO workflow.workflow_history (sub_module_id, pending_with, pending_with_name, draft_id, status) VALUES (:applicationId, :userId, :userName, :draftId, 'Draft')", nativeQuery = true)
//	void saveIntoWorkflowHistory(Long applicationId, Long userId, String userName, Long draftId);


	//deleting Draft Document
    @Query("SELECT d.id FROM DraftDocument d WHERE d.applicationNo = :applicationNo")
    Long getIdByApplicationNo(@Param("applicationNo") String applicationNo);

    @Modifying
    @Transactional
    @Query("UPDATE DraftDocument d SET d.isDeleted = true, d.isActive = false WHERE d.id = :draftId")
    void softDeleteById(@Param("draftId") Long draftId);

}
