package com.dsp.workflow.levelbyabbraction.service;


import com.dsp.workflow.levelbyabbraction.dto.LevelByAbbrActionDTO;
import com.dsp.workflow.levelbyabbraction.entity.LevelByAbbrAction;
import com.dsp.workflow.levelbyabbraction.repository.LevelByAbbrActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LevelByAbbrActionServiceImpl implements LevelByAbbrActionService {

    @Autowired
    private LevelByAbbrActionRepository levelByAbbrActionRepository;

    @Override
    public LevelByAbbrActionDTO createLevelByAbbrAction(LevelByAbbrActionDTO levelByAbbrActionDTO) {
        LevelByAbbrAction levelByAbbrAction = LevelByAbbrAction.builder()
                .roleId(levelByAbbrActionDTO.getRoleId())
                .level(levelByAbbrActionDTO.getLevel())
                .actionKey(levelByAbbrActionDTO.getActionKey())
                .abbr(levelByAbbrActionDTO.getAbbr())
                .stepType(levelByAbbrActionDTO.getStepType())
                .status(levelByAbbrActionDTO.getStatus())
                .statusKey(levelByAbbrActionDTO.getStatusKey())
                .applicationId(levelByAbbrActionDTO.getApplicationId())
                .build();

        LevelByAbbrAction savedLevelByAbbrAction = levelByAbbrActionRepository.save(levelByAbbrAction);
        return mapToDto(savedLevelByAbbrAction);
    }

    @Override
    public LevelByAbbrActionDTO getLevelByAbbrActionById(Long id) {
        LevelByAbbrAction levelByAbbrAction = levelByAbbrActionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LevelByAbbrAction not found with the given ID"));
        return mapToDto(levelByAbbrAction);
    }

    @Override
    public List<LevelByAbbrActionDTO> getAllLevelByAbbrActions() {
        return levelByAbbrActionRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public LevelByAbbrActionDTO updateLevelByAbbrAction(Long id, LevelByAbbrActionDTO levelByAbbrActionDTO) {
        return levelByAbbrActionRepository.findById(id)
                .map(existingLevelByAbbrAction -> {
                    existingLevelByAbbrAction.setRoleId(levelByAbbrActionDTO.getRoleId());
                    existingLevelByAbbrAction.setLevel(levelByAbbrActionDTO.getLevel());
                    existingLevelByAbbrAction.setActionKey(levelByAbbrActionDTO.getActionKey());
                    existingLevelByAbbrAction.setAbbr(levelByAbbrActionDTO.getAbbr());
                    existingLevelByAbbrAction.setStepType(levelByAbbrActionDTO.getStepType());
                    existingLevelByAbbrAction.setStatus(levelByAbbrActionDTO.getStatus());
                    existingLevelByAbbrAction.setStatusKey(levelByAbbrActionDTO.getStatusKey());
                    existingLevelByAbbrAction.setApplicationId(levelByAbbrActionDTO.getApplicationId());

                    LevelByAbbrAction savedLevelByAbbrAction = levelByAbbrActionRepository.save(existingLevelByAbbrAction);
                    return mapToDto(savedLevelByAbbrAction);
                }).orElseThrow(() -> new RuntimeException("LevelByAbbrAction not found with id " + id));
    }

//    @Override
//    public void deleteLevelByAbbrAction(Long id) {
//        if (levelByAbbrActionRepository.existsById(id)) {
//            levelByAbbrActionRepository.set.isdeleted(true);
//        } else {
//            throw new RuntimeException("LevelByAbbrAction not found with id " + id);
//        }
//    }

    @Override
    public void deleteLevelByAbbrAction(Long id) {
        Optional<LevelByAbbrAction> entity = levelByAbbrActionRepository.findById(id);
        if (entity.isPresent()) {
            LevelByAbbrAction level = entity.get();
            level.setIsDeleted(true);  // Assuming `isDeleted` is a Boolean field in your entity
            levelByAbbrActionRepository.save(level);  // Save the updated entity
        } else {
            throw new RuntimeException("LevelByAbbrAction not found with id " + id);
        }
    }

    @Override
    public List<LevelByAbbrActionDTO> getLevelByAbbrActionsByLevelAndApplicationIdAndRoleId(String level, Long applicationId, Integer roleId) {
        List<LevelByAbbrAction> actions = levelByAbbrActionRepository.findByLevelAndApplicationIdAndRoleId(level, applicationId, roleId);
        return actions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    private LevelByAbbrActionDTO mapToDto(LevelByAbbrAction levelByAbbrAction) {
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
