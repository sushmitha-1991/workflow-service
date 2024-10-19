package com.dsp.workflow.levelbyabbraction.repository;


import com.dsp.workflow.levelbyabbraction.entity.LevelByAbbrAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LevelByAbbrActionRepository  extends JpaRepository<LevelByAbbrAction, Long> {

    List<LevelByAbbrAction> findByLevelAndApplicationIdAndRoleId(String level, Long applicationId, Integer roleId);


}
