package com.dsp.workflow.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.dsp.workflow.entity.BaseUser;
import com.dsp.workflow.repository.MyUserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@Component
public class Utils {

	@Autowired
	private MyUserRepository myUserRepository;

	@Autowired
	private EntityManager entityManager; // For native query execution

	/**
	 * Fetches the user ID of the currently authenticated user
	 * 
	 * @return userId
	 */
	public Long getUserIdFromAuthentication() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new IllegalStateException("No authenticated user found");
		}

		String username = authentication.getName();
		BaseUser baseUser = myUserRepository.findByUsername(username);
		if (baseUser == null) {
			throw new UsernameNotFoundException("Authenticated user not found in the database");
		}

		return baseUser.getId();
	}

	/**
	 * Fetches the user ID of the currently authenticated user
	 * 
	 * @return userId
	 */
	public String getUserFullNameFromAuthentication() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new IllegalStateException("No authenticated user found");
		}

		String username = authentication.getName();
		BaseUser baseUser = myUserRepository.findByUsername(username);
		if (baseUser == null) {
			throw new UsernameNotFoundException("Authenticated user not found in the database");
		}

		// Concatenate firstName, middleName, and lastName
		String firstName = baseUser.getFirstName();
		String middleName = baseUser.getMiddleName() != null ? baseUser.getMiddleName() : ""; // Handle null middleName
		String lastName = baseUser.getLastName();

		// Construct full name by trimming any extra spaces
		return String.join(" ", firstName, middleName, lastName).trim();
	}

	/**
	 * Fetches the role ID of the authenticated user by joining user_roles and users
	 * table
	 * 
	 * @return roleId
	 */
	public Long getRoleIdByUserId() {
		Long userId = getUserIdFromAuthentication(); // Get the authenticated user's ID

		String nativeQuery = "SELECT ur.role_id FROM authentication.user_roles ur "
				+ "JOIN authentication.base_user u ON ur.user_id = u.id "
				+ "WHERE u.id = :userId AND ur.is_active = true AND ur.is_deleted = false";

		Query query = entityManager.createNativeQuery(nativeQuery);
		query.setParameter("userId", userId);

		try {
			// Fetch the result and ensure it's correctly cast to Long
			Object result = query.getSingleResult();

			if (result instanceof Integer) {
				// Convert Integer to Long
				return ((Integer) result).longValue();
			} else if (result instanceof Long) {

				// If it's already Long, return it directly
				return (Long) result;
			} else {
				throw new IllegalStateException("Unexpected data type for role_id: " + result.getClass());
			}
		} catch (Exception e) {
			throw new IllegalStateException("No active role found for the user with ID: " + userId, e);
		}
	}

	/**
	 * Fetches the role IDs of the authenticated user by joining user_roles and
	 * users table.
	 * 
	 * @return List<Long> - a list of role IDs
	 */
	public List<Long> getRoleIdsByUserId() {
		Long userId = getUserIdFromAuthentication(); // Get the authenticated user's ID

		String nativeQuery = "SELECT ur.role_id FROM authentication.user_roles ur "
				+ "JOIN authentication.base_user u ON ur.user_id = u.id "
				+ "WHERE u.id = :userId AND ur.is_active = true AND ur.is_deleted = false";

		Query query = entityManager.createNativeQuery(nativeQuery);
		query.setParameter("userId", userId);

		try {
			// Fetch the result list (which may contain multiple roles)
			List<Object> resultList = query.getResultList();

			// Convert the list of Object into a list of Longs
			List<Long> roleIds = new ArrayList<>();
			for (Object result : resultList) {
				if (result instanceof Integer) {
					// Convert Integer to Long
					roleIds.add(((Integer) result).longValue());
				} else if (result instanceof Long) {
					// Add Long values directly
					roleIds.add((Long) result);
				} else {
					throw new IllegalStateException("Unexpected data type for role_id: " + result.getClass());
				}
			}

			return roleIds;

		} catch (Exception e) {
			throw new IllegalStateException("No active roles found for the user with ID: " + userId, e);
		}
	}

	public Long getHouseIdFromAuthentication() {
		Long userId = getUserIdFromAuthentication(); // Get the authenticated user's ID

		// Define the native query
		String nativeQuery = "SELECT u.house_id FROM authentication.base_user u " + "WHERE u.id = :userId";

		// Execute the query
		Query query = entityManager.createNativeQuery(nativeQuery);
		query.setParameter("userId", userId);

		// Retrieve and return the house_id
		Object result = query.getSingleResult();
		if (result != null) {
			return ((Number) result).longValue(); // Cast result to Long
		} else {
			throw new IllegalStateException("House ID not found for the authenticated user.");
		}
	}

}
