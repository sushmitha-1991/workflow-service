/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dsp.workflow.entity.TaskHistory;

public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {
	// Custom queries can be added here if needed
}
