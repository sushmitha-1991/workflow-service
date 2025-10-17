/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dsp.workflow.entity.ApplicationInstance;

/**
 * 
 */
@Repository
public interface ApplicationInstanceRepository extends JpaRepository<ApplicationInstance, Long> {

	@Query(value = """
									SELECT
			    lsm.id AS lokSabhaNo,
			    lssm.id AS lokSabhaSessionNo,
			    rssm.id AS rajyaSabhaSessionNo,
			    ai.application_id AS documentTypeId,
			CAST(ai.document_date AS DATE) AS documentDate,
			    ai.html_content_en AS inputContent,
			    a.translate_flag AS translateFlag,
			    ai.starting_page_no as startingPageNo,
			    oom.document_key as docUuid,
			    oom.dsp2_key as dsp2key
			FROM master.application_instance ai
			LEFT JOIN master.applications a ON a.id = ai.application_id
			left join master.lok_sabha_masters lsm on lsm.lok_sabha_number = ai.lok_sabha_session
			left join master.lok_sabha_session_masters lssm
				on lssm.session_number = ai.lok_sabha_session_number and lssm.lok_sabha_master_id = lsm.id
			left join master.rajya_sabha_session_master rssm on rssm.session_number = ai.rajya_sabha_session
			left join onlyoffice.only_office_metadata oom on oom.dsp2_key = ai.only_office_dsp2_key
			WHERE ai.id = :applicationInstanceId

									""", nativeQuery = true)
	List<Object[]> getTranslationDataByApplicationInstanceId(
			@Param("applicationInstanceId") Long applicationInstanceId);

	/**
	 * @param applicationInstanceId
	 * @return
	 */
	@Query(value = """
						SELECT
			    lsm.id AS lokSabhaNo,
			    lssm.id AS lokSabhaSessionNo,
			    rssm.id AS rajyaSabhaSessionNo,
			    ai.application_id AS documentTypeId,
			CAST(ai.document_date AS DATE) AS documentDate,
			    ai.html_content_en AS inputContent,
			    ai.only_office_dsp2_key as dsp2Key
			FROM master.application_instance ai
			left join master.lok_sabha_masters lsm on lsm.lok_sabha_number = ai.lok_sabha_session
			left join master.lok_sabha_session_masters lssm on lssm.session_number = ai.lok_sabha_session_number
			left join master.rajya_sabha_session_master rssm on rssm.session_number = ai.rajya_sabha_session
			WHERE ai.id = :applicationInstanceId

						""", nativeQuery = true)
	List<Object[]> getCorrectedDebatesByApplicationInstanceId(Long applicationInstanceId);

	@Query(value = " select ai.html_content_en as enContent,ai.html_content_hi as hiContent from master.application_instance ai "
			+ "where ai.application_id =:applicationId and ai.application_no =:applicationNo", nativeQuery = true)
	Map<String, Object> getByApplicationIdAndApplicationNo(@Param("applicationId") Long applicationId,
			@Param("applicationNo") String applicationNo);

	/**
	 * @param applicationInstanceId
	 * @return
	 */
	@Query(value = """
						SELECT
			    lsm.id AS lokSabhaNo,
			    lssm.id AS lokSabhaSessionNo,
			    rssm.id AS rajyaSabhaSessionNo,
					CAST(ai.document_date AS DATE) AS debateDate,
			    ai.html_content_en AS content
			FROM master.application_instance ai
			left join master.lok_sabha_masters lsm on lsm.lok_sabha_number = ai.lok_sabha_session
			left join master.lok_sabha_session_masters lssm on lssm.session_number = ai.lok_sabha_session_number
			left join master.rajya_sabha_session_master rssm on rssm.session_number = ai.rajya_sabha_session
			WHERE ai.id = :applicationInstanceId

						""", nativeQuery = true)
	List<Object[]> getDebateTaggingByApplicationInstanceId(Long applicationInstanceId);

	/**
	 * @param applicationInstanceId
	 * @return
	 */
	@Query(value = """
			select ai.rajya_sabha_session as rajyaSabhaSessionNo,
			ai.application_id as documentTypeId,
			ai.document_date as documentDate,
			bi."content" as inputContent,
			bi.subject as subject,
			bi.bulletin_no as bulletinNo
			from master.application_instance ai
			left join bulletin2.bulletin2 b on ai.ref_id = b.id
			left join bulletin2.bulletin2_input bi on bi.generated_bulletin2_id = b.id
			where ai.ref_id = :applicationInstanceId

									""", nativeQuery = true)
	List<Object[]> getSectionBulletin2ByApplicationInstanceId(Long applicationInstanceId);

	/**
	 * @param applicationInstanceId
	 * @return
	 */
	@Query(value = """
			SELECT
						    a."name" as applicationName,
						    h.abbr as house
						FROM master.application_instance ai
						left join master.applications a on a.id = ai.application_id
						left join master.modules m on m.id = a.module_id
						left join master.houses h on h.id = m.house_id
						WHERE ai.id = :applicationInstanceId  and h.abbr = 'RS'

						""", nativeQuery = true)
	List<Object[]> getSectionB2AppHouse(Long applicationInstanceId);

	@Query(value = """
				select ai.application_id, ai.current_status, ai.document_date
				from master.application_instance ai
				where ai.application_id = :applicationId
				and ai.document_date = :docDate
				and ai.is_active = true
				order by ai.created_at desc limit 1
			""", nativeQuery = true)
	List<Object[]> getDocStatusByDate(Long applicationId, LocalDate docDate);

	/**
	 * @return
	 */
	@Query(value = "SELECT id FROM master.rajya_sabha_session_master ORDER BY session_start DESC LIMIT 1", nativeQuery = true)
	Long findLatestByCreatedBy();

	/**
	 * @param refId
	 * @return
	 */
	Optional<ApplicationInstance> findByRefId(Long refId);

	/**
	 * @param applicationInstanceId
	 * @return
	 */
	@Query(value = "SELECT application_no from master.application_instance where id = :applicationInstanceId", nativeQuery = true)
	String getRefNo(Long applicationInstanceId);

	/**
	 * @param applicationInstanceId
	 * @return
	 */
	@Query(value = "SELECT a.name from master.application_instance ai LEFT JOIN master.applications a on a.id = ai.application_id where ai.id", nativeQuery = true)
	String getModuleName(Long applicationInstanceId);

}
