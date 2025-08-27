/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.notesheet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dsp.workflow.notesheet.entity.Notesheet;

@Repository
public interface NotesheetRepository extends JpaRepository<Notesheet, Long> {

	@Query(value = """
			SELECT CONCAT(d.abbr, ' : ', d.description)
						FROM master.designations d
						JOIN authentication.base_user bu ON bu.designation_id = d.id
						WHERE bu.id = :userId
						""", nativeQuery = true)
	String findRoleNameByUserId(@Param("userId") Long userId);

	List<Notesheet> findByApplicationInstanceId(Long applicationInstanceId);

	/**
	 * @param applicationInstanceId
	 * @return
	 */
	@Query(value = "SELECT ai.application_no AS referenceNo " + "FROM workflow.notesheet n "
			+ "LEFT JOIN master.application_instance ai ON n.application_instance_id = ai.id "
			+ "WHERE n.application_instance_id = :applicationInstanceId LIMIT 1", nativeQuery = true)
	String findNotesheetsWithReferenceNo(@Param("applicationInstanceId") Long applicationInstanceId);

}
