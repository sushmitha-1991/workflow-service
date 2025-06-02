package com.dsp.workflow.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dsp.workflow.entity.PublishDocumentTemp;
import com.dsp.workflow.entity.PublishHistoryEntity;

public interface PublishRepository
        extends JpaRepository<PublishHistoryEntity, Long>, JpaSpecificationExecutor<PublishHistoryEntity> {
    @Query("SELECT ph FROM PublishHistoryEntity ph WHERE ph.applicationNo = :applicationNo")
    PublishHistoryEntity findByApplicationNo(@Param("applicationNo") String applicationNo);

    Optional<PublishHistoryEntity> findByForDate(LocalDate forDate);

    Optional<PublishHistoryEntity> findByForDateAndApplicationName(LocalDate forDate, String applicationName);

    Optional<PublishHistoryEntity> findByApplicationNameAndRajyaSabhaSessionNo(String applicationName, String rsNo);

    Optional<PublishHistoryEntity> findByApplicationNameAndLokSabhaSessionNo(String applicationName, String sessNo);

    Optional<PublishHistoryEntity> findByApplicationName(String applicationName);

    @Query(value = "select a.primary_office_id in ( select office_id from authentication.user_roles where user_id = :userId and is_active = true and is_deleted = false) from master.applications a where a.id=:appId", nativeQuery = true)
    Boolean findOfficeIdByUserId(@Param("userId") Long userId,
            @Param("appId") Long appId);

    List<PublishHistoryEntity> findByApplicationIdAndRajyaSabhaSessionNo(Long appId, String sesNo);

    List<PublishHistoryEntity> findByApplicationIdAndLokSabhaNoAndLokSabhaSessionNo(Long appId, String lsNo,
            String sesNo);

}
