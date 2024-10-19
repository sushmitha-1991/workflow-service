package com.dsp.workflow.levelbyabbraction.mapper;

import com.dsp.workflow.levelbyabbraction.dto.LevelByAbbrActionDTO;
import com.dsp.workflow.levelbyabbraction.entity.LevelByAbbrAction;

public class LevelByAbbrActionMapper {

    // Map DTO to Entity
    public static LevelByAbbrAction toEntity(LevelByAbbrActionDTO levelByAbbrActionDTO) {
        return LevelByAbbrAction.builder()
                .roleId(levelByAbbrActionDTO.getRoleId())
                .level(levelByAbbrActionDTO.getLevel())
                .actionKey(levelByAbbrActionDTO.getActionKey())
                .abbr(levelByAbbrActionDTO.getAbbr())
                .stepType(levelByAbbrActionDTO.getStepType())
                .status(levelByAbbrActionDTO.getStatus())
                .statusKey(levelByAbbrActionDTO.getStatusKey())
                .applicationId(levelByAbbrActionDTO.getApplicationId())
                .build();
    }

    // Map Entity to DTO
    public static LevelByAbbrActionDTO toDTO(LevelByAbbrAction levelByAbbrAction) {
        return LevelByAbbrActionDTO.builder()
                .id(levelByAbbrAction.getId())
                .roleId(levelByAbbrAction.getRoleId())
                .level(levelByAbbrAction.getLevel())
                .actionKey(levelByAbbrAction.getActionKey())
                .abbr(levelByAbbrAction.getAbbr())
                .stepType(levelByAbbrAction.getStepType())
                .status(levelByAbbrAction.getStatus())
                .statusKey(levelByAbbrAction.getStatusKey())
                .applicationId(levelByAbbrAction.getApplicationId())
                .build();
    }
}
