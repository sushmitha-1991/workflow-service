package com.dsp.workflow.levelbyabbraction.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dsp.workflow.levelbyabbraction.entity.LevelByAbbrAction;

@Repository
public interface LevelByAbbrActionRepository extends JpaRepository<LevelByAbbrAction, Long> {

	List<LevelByAbbrAction> findByLevelAndApplicationIdAndRoleId(String level, Long applicationId, Integer roleId);

	/**
	 * @param level
	 * @param applicationId
	 * @param roleIds
	 * @return //
	 */
//	List<LevelByAbbrAction> findByLevelAndApplicationIdAndRoleIds(String level, Long applicationId,
//			List<Integer> roleIds);

//	@Query("SELECT l FROM LevelByAbbrAction l WHERE l.level = :level AND l.applicationId = :applicationId AND l.roleId IN :roleIds AND l.status = 'Active'")
//	List<LevelByAbbrAction> findByLevelAndApplicationIdAndRoleIdIn(@Param("level") String level,
//			@Param("applicationId") Long applicationId, @Param("roleIds") List<Long> roleIds);
	
	@Query("SELECT l FROM LevelByAbbrAction l WHERE l.level = :level AND l.applicationId = :applicationId AND l.roleId IN :roleIds AND l.isActive = true AND l.isDeleted = false")
	List<LevelByAbbrAction> findByLevelAndApplicationIdAndRoleIdIn(@Param("level") String level,
	                                                               @Param("applicationId") Long applicationId, 
	                                                               @Param("roleIds") List<Long> roleIds);


}
