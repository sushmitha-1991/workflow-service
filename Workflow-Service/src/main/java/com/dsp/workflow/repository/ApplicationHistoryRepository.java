/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dsp.workflow.entity.ApplicationHistory;

/**
 * 
 */
public interface ApplicationHistoryRepository extends JpaRepository<ApplicationHistory, Long> {

}
