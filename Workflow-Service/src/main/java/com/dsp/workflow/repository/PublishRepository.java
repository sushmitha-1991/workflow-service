package com.dsp.workflow.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dsp.workflow.entity.PublishHistoryEntity;

import jakarta.transaction.Transactional;

public interface PublishRepository
		extends JpaRepository<PublishHistoryEntity, Long>, JpaSpecificationExecutor<PublishHistoryEntity> {
	@Query("SELECT ph FROM PublishHistoryEntity ph WHERE ph.applicationNo = :applicationNo")
	PublishHistoryEntity findByApplicationNo(@Param("applicationNo") String applicationNo);

	Optional<PublishHistoryEntity> findByForDate(LocalDate forDate);

	Optional<PublishHistoryEntity> findByForDateAndApplicationName(LocalDate forDate, String applicationName);

	Optional<PublishHistoryEntity> findByApplicationNameAndRajyaSabhaSessionNo(String applicationName, String rsNo);

	Optional<PublishHistoryEntity> findByApplicationNameAndLokSabhaSessionNo(String applicationName, String sessNo);

	Optional<PublishHistoryEntity> findByApplicationName(String applicationName);

	@Query(value = "select a.primary_office_id in ( select office_id from authentication.user_roles where user_id = :userId and is_active = true and is_deleted = false) from master.applications a where a.id=:appId", nativeQuery = true)
	Boolean findOfficeIdByUserId(@Param("userId") Long userId, @Param("appId") Long appId);

	@Query(value = "select abbr from master.applications a where a.id=:appId", nativeQuery = true)
	String getAbbrByApplicationId(@Param("appId") Long appId);

	List<PublishHistoryEntity> findByApplicationIdAndRajyaSabhaSessionNo(Long appId, String sesNo);

	List<PublishHistoryEntity> findByApplicationIdAndLokSabhaNoAndLokSabhaSessionNo(Long appId, String lsNo,
			String sesNo);

	@Query(value = "select lssm.session_number as sesNo,lsm.lok_sabha_number as lsNo from master.lok_sabha_session_masters lssm "
			+ "left join master.lok_sabha_masters lsm on lssm.lok_sabha_master_id =lsm.id "
			+ "order by lssm.session_start desc limit 1", nativeQuery = true)
	Map<String, Object> findLatestLS();

	@Query(value = "select rssm.session_number from master.rajya_sabha_session_master rssm "
			+ " order by rssm.session_start desc limit 1", nativeQuery = true)

	String findLatestRS();

	@Query(value = "SELECT o.office_name FROM master.applications a "
			+ "JOIN master.offices o ON o.id = a.primary_office_id " + "WHERE a.id = :appId", nativeQuery = true)
	String getPrimaryOfficeNameByApplicationId(@Param("appId") Long appId);

	@Query(value = "SELECT abbr FROM master.houses  " + "WHERE id = :houseId", nativeQuery = true)
	Optional<String> findHouseById(Long houseId);

	@Query(value = "SELECT ai.id FROM master.application_instance ai WHERE ai.application_no = :applicationNo", nativeQuery = true)
	Long findApplicationInstanceIdsByAppNo(@Param("applicationNo") String applicationNo);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = """
			UPDATE master.application_instance
			SET current_status = 'Unpublished',
			    is_published = false
			WHERE application_no = :applicationNo
			""", nativeQuery = true)
	void updateUnpublishStatus(@Param("applicationNo") String applicationNo);

	/**
	 * @param applicationInstId
	 */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query(value = """
			UPDATE workflow.workflow_history
			SET status = 'Unpublished'
			WHERE application_instance_id = :applicationInstId
			  AND status = 'Published'
			""", nativeQuery = true)
	void updateUnpublishStatusInWorkflowHistory(@Param("applicationInstId") Long applicationInstId);

	/**
	 * @param applicationId
	 * @return
	 */
	@Query(value = """
			select dsp1_document_name
			from master.applications
			WHERE id = :applicationId
			""", nativeQuery = true)
	String getDsp1DocName(@Param("applicationId") Long applicationId);

	/**
	 * @param applicationId
	 * @return
	 */
	@Query(value = """
			select abbr
			from master.applications
			WHERE id = :applicationId
			""", nativeQuery = true)
	String getAppAbbr(@Param("applicationId") Long applicationId);

	@Query(value = """
			select ai.ref_id
			from master.application_instance ai
			WHERE ai.application_no = :refNoCNUR377
			""", nativeQuery = true)
	Long getRefIDByApplicationNo(@Param("refNoCNUR377") String refNoCNUR377);

	@Query(value = """
	        SELECT pd.mp_code
	        FROM notices.notice_details nd
	        JOIN members.personal_details pd 
	            ON nd.member_id = pd.id
	        WHERE nd.id = :cnur377Id
	        """, nativeQuery = true)
	String getMpCodeByCNUR377Id(@Param("cnur377Id") Long cnur377Id);


	@Transactional
	@Modifying
	@Query(value = """
			UPDATE section_login.uploaded_document
			SET is_active = false,
			    is_deleted = true
			WHERE receiver_id = :mpCode
			  AND document_uuid = :docUUID
			""", nativeQuery = true)
	int unPublishFromUploadedDocuments(@Param("mpCode") String mpCode, @Param("docUUID") String docUUID);

	@Query(value = """
			select ph.lok_sabha_no,ph.lok_sabha_session_no,ph.for_date,ph.application_name ,ph.application_id,ph.application_no,ph.locale ,ph.publish_url,o.download_url from workflow.publish_history ph
			left join master.applications a on a.id=ph.application_id
			left join master.application_instance a2 on a2.application_no = REGEXP_REPLACE(ph.application_no, '_(HI|EN)$', '')
			left join onlyoffice.only_office_metadata o on o.dsp2_key =a2.only_office_dsp2_key
			where ph.application_id in (34,31,33,63) and ph.house=CAST(:house AS varchar)
			 and (CAST(:lokSabhaNo AS varchar) is null or ph.lok_sabha_no = CAST(:lokSabhaNo AS varchar))
			  and (
			     CAST(:sessionNo AS varchar) is null
			     or (:house = 'LS' and ph.lok_sabha_session_no = CAST(:sessionNo AS varchar))
			     or (:house = 'RS' and ph.rajya_sabha_session_no = CAST(:sessionNo AS varchar))
			   )
			 and (CAST(:forDate AS DATE) is null or ph.for_date = CAST(:forDate AS DATE))
			 and (CAST(:applicationId AS bigint) is null or ph.application_id = CAST(:applicationId AS bigint))
			 and ph.publish_status =true and ph.is_active =true
			 order by ph.for_date desc
						""", 
			countQuery = """
					select count(*)
					from workflow.publish_history ph
					where ph.application_id in (34,31,33,63) and ph.house=CAST(:house AS varchar)
					 and (CAST(:lokSabhaNo AS varchar) is null or ph.lok_sabha_no = CAST(:lokSabhaNo AS varchar))
					  and (
					     CAST(:sessionNo AS varchar) is null
					     or (:house = 'LS' and ph.lok_sabha_session_no = CAST(:sessionNo AS varchar))
					     or (:house = 'RS' and ph.rajya_sabha_session_no = CAST(:sessionNo AS varchar))
					   )
					 and (CAST(:forDate AS DATE) is null or ph.for_date = CAST(:forDate AS DATE))
					 and (CAST(:applicationId AS bigint) is null or ph.application_id = CAST(:applicationId AS bigint))
					  and ph.publish_status = true
					  and ph.is_active = true
			""", nativeQuery = true)
	Page<Object[]> getPublishedDocuments(@Param("lokSabhaNo") String lokSabhaNo, @Param("sessionNo") String sessionNo,
			@Param("forDate") LocalDate forDate, @Param("applicationId") Long applicationId,
			@Param("house") String house,Pageable pageable);
}
