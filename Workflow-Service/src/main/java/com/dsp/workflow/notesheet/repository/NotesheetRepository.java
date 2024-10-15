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
			SELECT CONCAT(r.description, ' : ', r.role_name)
			FROM authentication.user_roles ur
			JOIN authentication.roles r ON ur.role_id = r.id
			WHERE ur.user_id = :userId
			  AND ur.is_active = true
			  AND ur.is_deleted = false
			  AND r.is_active = true
			  AND r.is_deleted = false
			LIMIT 1
			""", nativeQuery = true)
	String findRoleNameByUserId(@Param("userId") Long userId);

	List<Notesheet> findByApplicationInstanceId(Long applicationInstanceId);

}
