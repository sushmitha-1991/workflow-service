/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dsp.workflow.entity.Application;


/**
 * 
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

	@Query(value="select coalesce(document_display_name, name) as name from master.applications where id = :id", 
		   nativeQuery = true)
	String findApplicationDisplayName(@Param("id") Long id);

	@Query(value=""" 
				select a.id from master.applications a
				left join master.modules m on m.id = a.module_id 
				where a.abbr = :abbr
				and m.house_id = :houseId 
				and a.is_active = true
				""", nativeQuery = true)
	Long findIdByAbbr(@Param("abbr") String abbr, @Param("houseId") Long houseId);

}

