package com.dsp.workflow.mapper;

import com.dsp.workflow.dto.PublishHistoryDTO;
import com.dsp.workflow.entity.PublishHistoryEntity;

public class PublishMapper {

    public static PublishHistoryDTO toDTO(PublishHistoryEntity entity) {
        PublishHistoryDTO dto = PublishHistoryDTO.builder()
                .id(entity.getId())
                .lokSabhaNo(entity.getLokSabhaNo())
                .lokSabhaSessionNo(entity.getLokSabhaSessionNo())
                .rajyaSabhaSessionNo(entity.getRajyaSabhaSessionNo())
                .applicationId(entity.getApplicationId())
                .applicationName(entity.getApplicationName())
                .applicationNo(entity.getApplicationNo())
                .publishUuid(entity.getPublishUuid())
                .publishUrl(entity.getPublishUrl())
                .fileType(entity.getFileType())
                .forDate(entity.getForDate())
                .publishStatus(entity.getPublishStatus())
                .publishByUser(entity.getPublishByUser())
                .publishDate(entity.getPublishDate())
                .unpublishByUser(entity.getUnpublishByUser())
                .unpublishDate(entity.getUnpublishDate())
                .officeId(entity.getOfficeId())
                .locale(entity.getLocale())
                .build();
        return dto;
    }

    public static PublishHistoryEntity toEntity(PublishHistoryDTO dto) {
        PublishHistoryEntity entity = PublishHistoryEntity.builder()
                .id(dto.getId())
                .lokSabhaNo(dto.getLokSabhaNo())
                .lokSabhaSessionNo(dto.getLokSabhaSessionNo())
                .rajyaSabhaSessionNo(dto.getRajyaSabhaSessionNo())
                .applicationId(dto.getApplicationId())
                .applicationName(dto.getApplicationName())
                .applicationNo(dto.getApplicationNo())
                .publishUuid(dto.getPublishUuid())
                .publishUrl(dto.getPublishUrl())
                .fileType(dto.getFileType())
                .forDate(dto.getForDate())
                .publishStatus(dto.getPublishStatus())
                .publishByUser(dto.getPublishByUser())
                .publishDate(dto.getPublishDate())
                .unpublishByUser(dto.getUnpublishByUser())
                .unpublishDate(dto.getUnpublishDate())
                .officeId(dto.getOfficeId())
                .locale(dto.getLocale())
                .build();
        return entity;
    }

}
