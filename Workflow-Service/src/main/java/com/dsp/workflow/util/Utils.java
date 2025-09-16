package com.dsp.workflow.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.dsp.workflow.dto.UserDetailsDTO;
import com.dsp.workflow.entity.BaseUser;
import com.dsp.workflow.repository.ApplicationRepository;
import com.dsp.workflow.repository.MyUserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class Utils {

	@Autowired
	private MyUserRepository myUserRepository;

	@Autowired
	private ApplicationRepository appRepository;

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

	public static boolean isEmpty(String input) {

		if (input == null || input.equals("")) {
			return true;
		}
		return false;
	}

	public String getHouseAbbr() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (!(principal instanceof UserDetailsDTO udto)) {
			throw new RuntimeException("Authentication Problems");
		}
		return udto.getHouseAbbr();
	}

	public Long getApplicationId(String abbr) {

		if (StringUtils.isEmpty(abbr))
			return null;

		Long appId = appRepository.findIdByAbbr(abbr, getHouseIdFromAuthentication());
		return appId;
	}

	/**
	 * Extracts the IP address of the logged-in user from the JWT token in the
	 * request.
	 *
	 * @param request HttpServletRequest containing the "Authorization" header
	 * @return IP address if present, otherwise null
	 */
	public static String getLoggedInUserIp(HttpServletRequest request) {
		String token = getTokenFromRequest(request);
		if (token == null) {
			return null;
		}
		return extractIpFromJwt(token);
	}

	// Step 1: Extract token from header
	private static String getTokenFromRequest(HttpServletRequest request) {
		String authorizationHeader = request.getHeader("Authorization");
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			return authorizationHeader.substring(7);
		}
		return null;
	}

	// Step 2: Decode token payload and extract "ip" claim
	private static String extractIpFromJwt(String jwt) {
		String[] parts = jwt.split("\\.");
		if (parts.length < 2)
			return null;

		try {
			String payloadB64 = parts[1];
			byte[] decoded = Base64.getUrlDecoder().decode(padBase64(payloadB64));
			String payloadJson = new String(decoded, StandardCharsets.UTF_8);

			int ipIndex = payloadJson.indexOf("\"ip\"");
			if (ipIndex != -1) {
				int colon = payloadJson.indexOf(":", ipIndex);
				int quote1 = payloadJson.indexOf("\"", colon);
				int quote2 = payloadJson.indexOf("\"", quote1 + 1);
				return payloadJson.substring(quote1 + 1, quote2);
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	// Helper for Base64 padding
	private static String padBase64(String b64) {
		int remainder = b64.length() % 4;
		if (remainder == 0)
			return b64;
		return b64 + "=".repeat(4 - remainder);
	}

}
