/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.repository;

import java.util.List;
import java.util.Map;

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
			    ai.starting_page_no as startingPageNo
			FROM master.application_instance ai
			LEFT JOIN master.applications a ON a.id = ai.application_id
			left join master.lok_sabha_masters lsm on lsm.lok_sabha_number = ai.lok_sabha_session
			left join master.lok_sabha_session_masters lssm on lssm.session_number = ai.lok_sabha_session_number
			left join master.rajya_sabha_session_master rssm on rssm.session_number = ai.rajya_sabha_session
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
			    ai.html_content_en AS inputContent
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

}
