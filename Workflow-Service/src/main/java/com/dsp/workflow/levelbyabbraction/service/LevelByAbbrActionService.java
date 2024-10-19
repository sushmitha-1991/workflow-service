package com.dsp.workflow.levelbyabbraction.service;

import java.util.List;

import com.dsp.workflow.levelbyabbraction.dto.LevelByAbbrActionDTO;

public interface LevelByAbbrActionService {

	LevelByAbbrActionDTO createLevelByAbbrAction(LevelByAbbrActionDTO levelByAbbrActionDTO);

	LevelByAbbrActionDTO getLevelByAbbrActionById(Long id);

	List<LevelByAbbrActionDTO> getAllLevelByAbbrActions();

	LevelByAbbrActionDTO updateLevelByAbbrAction(Long id, LevelByAbbrActionDTO levelByAbbrActionDTO);

	void deleteLevelByAbbrAction(Long id);

	/**
	 * @param level
	 * @param applicationId
	 * @param roleId
	 * @return
	 */
	List<LevelByAbbrActionDTO> getLevelByAbbrActionsByCriteria(String level, Long applicationId, Integer[] roleId);
}
