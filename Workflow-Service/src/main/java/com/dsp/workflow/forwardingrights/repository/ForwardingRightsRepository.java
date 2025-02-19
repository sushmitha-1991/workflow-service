package com.dsp.workflow.forwardingrights.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dsp.workflow.forwardingrights.entity.ForwardingRights;

@Repository
public interface ForwardingRightsRepository extends JpaRepository<ForwardingRights, Long> {

	// Optimized query to fetch both forwarding rights and user details in a single
	// query
	@Query(value = "SELECT u.id AS userId, u.first_name AS firstName, u.middle_name AS middleName, "
			+ "u.last_name AS lastName, u.email AS email, ur.office_id AS officeId, "
			+ "case when ar.abbr = 'SG' then null else of.office_name end AS officeName, "
			+ "ar.id AS roleId, ar.abbr AS roleName, ar.description AS description, fr.action_url as actionUrl, d.abbr as designation "
			+ "FROM workflow.forwarding_rights fr "
			+ "JOIN authentication.user_roles ur ON fr.receiver_role_id = ur.role_id "
			+ "JOIN authentication.base_user u ON ur.user_id = u.id "
			+ "LEFT JOIN authentication.roles ar ON ur.role_id = ar.id "
			+ "LEFT JOIN master.offices of ON ur.office_id = of.id "
			+ "LEFT JOIN master.designations d on d.id = u.designation_id "
			+ "WHERE fr.application_id = :applicationId " + "AND fr.sender_role_id IN :senderRoleIds "
			+ "AND u.house_id = :house " + "AND fr.action_id = :actionId AND u.id != :userId", nativeQuery = true)
	List<Object[]> findUserDetailsWithForwardingRights(@Param("applicationId") Long applicationId,
			@Param("senderRoleIds") List<Long> senderRoleIds, @Param("actionId") Long actionId, Long house,
			Long userId);
}
