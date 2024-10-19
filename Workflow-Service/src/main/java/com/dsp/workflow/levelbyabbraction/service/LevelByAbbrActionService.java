package com.dsp.workflow.levelbyabbraction.service;

import com.dsp.workflow.levelbyabbraction.dto.LevelByAbbrActionDTO;

import java.util.List;

public interface LevelByAbbrActionService {

    LevelByAbbrActionDTO createLevelByAbbrAction(LevelByAbbrActionDTO levelByAbbrActionDTO);

    LevelByAbbrActionDTO getLevelByAbbrActionById(Long id);

    List<LevelByAbbrActionDTO> getAllLevelByAbbrActions();

    LevelByAbbrActionDTO updateLevelByAbbrAction(Long id, LevelByAbbrActionDTO levelByAbbrActionDTO);

    void deleteLevelByAbbrAction(Long id);

    List<LevelByAbbrActionDTO> getLevelByAbbrActionsByLevelAndApplicationIdAndRoleId(String level, Long applicationId, Integer roleId);
}
