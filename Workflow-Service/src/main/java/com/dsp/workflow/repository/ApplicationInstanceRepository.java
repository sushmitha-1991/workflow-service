/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.repository;

import java.util.List;

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
			    ai.lok_sabha_session,
			    ai.lok_sabha_session_number,
			    ai.rajya_sabha_session,
			    ai.application_id,
			    CAST(ai.document_date AS DATE),
			    ai.html_content_en,
			    a.translate_flag
			FROM master.application_instance ai
			LEFT JOIN master.applications a ON a.id = ai.application_id
			WHERE ai.id = :applicationInstanceId

						""", nativeQuery = true)
	List<Object[]> getTranslationDataByApplicationInstanceId(
			@Param("applicationInstanceId") Long applicationInstanceId);

}
