package com.dsp.workflow.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.dsp.workflow.dto.PublishHistoryDTO;
import com.dsp.workflow.dto.UrlDto;
import com.dsp.workflow.entity.PublishHistoryEntity;
import com.dsp.workflow.mapper.PublishMapper;
import com.dsp.workflow.repository.PublishRepository;
import com.dsp.workflow.service.PublishService;

import java.lang.reflect.Field;

import com.dsp.workflow.dto.UnPublishRequest;


@Service
public class PublishServiceImpl implements PublishService {

    @Autowired
    private PublishRepository repository;

    @Override
    public PublishHistoryDTO save(PublishHistoryDTO dto) {
        System.out.println("DTO");
        System.out.println(dto);

        PublishHistoryEntity entity = PublishMapper.toEntity(dto);
        
        PublishHistoryEntity alreadyExists=repository.findByApplicationNo(entity.getApplicationNo());
        if(alreadyExists!=null)
        {
            System.out.println("application no exists  "+alreadyExists.getApplicationNo());
            Field[] fields = entity.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true); // Allow access to private fields
                try {
                    Object newValue = field.get(entity);
                    if (newValue != null) {
                        Field entityField = alreadyExists.getClass().getDeclaredField(field.getName());
                        entityField.setAccessible(true);
                        entityField.set(alreadyExists, newValue);
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException("Error updating field: " + field.getName(), e);
                }
            }
            entity = repository.save(alreadyExists);
        }
        else{
            System.out.println("application no  "+entity.getApplicationNo());

            entity = repository.save(entity);

        }

        System.out.println("@@@@@@@@@");
        System.out.println(entity);

        return PublishMapper.toDTO(entity);
    }

    @Override
    public UrlDto getByDate(LocalDate date, String documentName, String locale) {
        // PublishHistoryEntity entity=PublishMapper.toEntity(dto);
        Specification<PublishHistoryEntity> spec = Specification.where(isPublishStatus(true));
        spec = spec.and(hasForDate(date));
        spec=spec.and(hasApplicationName(documentName));
        Sort sort = Sort.by(Sort.Order.desc("forDate"));
        List<PublishHistoryEntity> responseEntitys=repository.findAll(spec);
        PublishHistoryEntity entity=responseEntitys.get(0);
        UrlDto responseDto = UrlDto.builder()
        .date(date.toString())
        .name(entity.getApplicationName())
        .fileType(entity.getFileType())
        .url(entity.getPublishUrl())
        .build();
        return responseDto;
    }
    @Override
    public PublishHistoryDTO update(Long id, PublishHistoryDTO dto) {
        
        PublishHistoryEntity existingEntity=repository.findById(id).get();

        PublishHistoryEntity updatedEntity=PublishMapper.toEntity(dto);


        Field[] fields = updatedEntity.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true); // Allow access to private fields
            try {
                Object newValue = field.get(updatedEntity);
                if (newValue != null) {
                    Field entityField = existingEntity.getClass().getDeclaredField(field.getName());
                    entityField.setAccessible(true);
                    entityField.set(existingEntity, newValue);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Error updating field: " + field.getName(), e);
            }
        }

        existingEntity=repository.save(existingEntity);
        
        return PublishMapper.toDTO(existingEntity);
    }
  

    @Override
    public PublishHistoryDTO updateById(Long id,UnPublishRequest request) {
        
        PublishHistoryEntity entity=repository.findById(id).get();
        entity.setUnpublishByUser(request.getUnpublishByUser());
        if(request.getUnpublishDate()==null)
        {
            request.setUnpublishDate(LocalDate.now());
        }
        entity.setUnpublishDate(request.getUnpublishDate());
        entity.setPublishStatus(false);
        entity=repository.save(entity);
        
        return PublishMapper.toDTO(entity);
    }

    @Override
    public PublishHistoryDTO getbyAppNo(String applicationNo) {
        System.out.println("@@@@@@@@");
        System.out.println(applicationNo);
        PublishHistoryEntity entity=repository.findByApplicationNo(applicationNo);
        System.out.println(entity);
        return PublishMapper.toDTO(entity);
    }
    @Override
    public UrlDto getAllUnpublished(LocalDate date, String applicationName, String locale) {
        Specification<PublishHistoryEntity> spec = Specification.where(isPublishStatus(false));
        spec = spec.and(hasForDate(date));
        spec=spec.and(hasApplicationName(applicationName));
        Sort sort = Sort.by(Sort.Order.desc("forDate"));
        List<PublishHistoryEntity> responseEntitys=repository.findAll(spec);
        PublishHistoryEntity entity=responseEntitys.get(0);
        UrlDto responseDto = UrlDto.builder()
        .date(date.toString())
        .name(entity.getApplicationName())
        .fileType(entity.getFileType())
        .url(entity.getPublishUrl())
        .build();
        return responseDto;

    }

    //Specifications
    public static Specification<PublishHistoryEntity> hasForDate(LocalDate date) {
        return (root, query, criteriaBuilder) -> {
            if (date != null) {
                return criteriaBuilder.equal(root.get("forDate"), date);
            }
            return criteriaBuilder.conjunction(); // Returns a valid Predicate that does not filter anything
        };
    }
    public static Specification<PublishHistoryEntity> isPublishStatus(Boolean publishStatus) {
        return (root, query, criteriaBuilder) -> {
            if (publishStatus != null) {
                return criteriaBuilder.equal(root.get("publishStatus"), publishStatus);
            }
            return criteriaBuilder.conjunction(); // No filtering if null
        };
    }
    public static Specification<PublishHistoryEntity> hasApplicationName(String applicationName) {
        return (root, query, criteriaBuilder) -> {
            if (applicationName != null) {
                return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("applicationName")), 
                    "%" + applicationName.toLowerCase() + "%"
                );
            }
            return criteriaBuilder.conjunction();
        };
    }
}
