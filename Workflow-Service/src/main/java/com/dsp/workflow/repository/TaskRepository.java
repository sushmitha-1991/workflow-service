package com.dsp.workflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dsp.workflow.entity.ApplicationInstance;

@Repository
public interface TaskRepository extends JpaRepository<ApplicationInstance, Long> {

	@Query(value = """
						SELECT ai.application_no AS refNo,
						       app.name AS documentName,
						       ai.created_at AS date,
						       ai.current_status AS status,
						    CONCAT(u.first_name, ' ', u.last_name, ' - ', r.abbr, ' - ', o.office_name) AS pendingAt,
						       '18' AS LSNo,
						       'III' AS SSNo
						FROM master.application_instance ai
						JOIN master.applications app ON ai.application_id = app.id
						JOIN authentication.base_user u ON u.id = :receiverUserId
			JOIN authentication.user_roles ur ON ur.user_id = u.id AND ur.is_active = true
			JOIN authentication.roles r ON r.id = ur.role_id
			JOIN master.offices o ON o.id = ur.office_id
			WHERE ai.id = :applicationInstanceId
						""", nativeQuery = true)
	List<Object[]> findTasksWithDetailsByReceiverUserIdAndApplicationInstanceId(Long receiverUserId,
			Long applicationInstanceId);
}
