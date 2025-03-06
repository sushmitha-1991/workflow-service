package com.dsp.workflow.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dsp.workflow.entity.PublishHistoryEntity;

public interface PublishRepository extends JpaRepository<PublishHistoryEntity,Long>,JpaSpecificationExecutor<PublishHistoryEntity> {
    @Query("SELECT ph FROM PublishHistoryEntity ph WHERE ph.applicationNo = :applicationNo")
    PublishHistoryEntity findByApplicationNo(@Param("applicationNo") String applicationNo);

    Optional<PublishHistoryEntity> findByForDate(LocalDate forDate);

    Optional<PublishHistoryEntity> findByForDateAndApplicationName(LocalDate forDate, String applicationName);


}
