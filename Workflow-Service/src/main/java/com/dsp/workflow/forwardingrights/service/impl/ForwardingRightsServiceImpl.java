/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.forwardingrights.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.dsp.workflow.exception.UserNotFoundException;
import com.dsp.workflow.forwardingrights.dto.ForwardingRightsDTO;
import com.dsp.workflow.forwardingrights.entity.ForwardingRights;
import com.dsp.workflow.forwardingrights.mapper.ForwardingRightsMapper;
import com.dsp.workflow.forwardingrights.repository.ForwardingRightsRepository;
import com.dsp.workflow.forwardingrights.service.ForwardingRightsService;
import com.dsp.workflow.response.GenericResponse;

/**
 * ForwardingRightsService Implementation
 */
@Service
public class ForwardingRightsServiceImpl implements ForwardingRightsService {

	@Autowired
	private ForwardingRightsRepository repository;

	@Autowired
	private ForwardingRightsMapper forwardingRightsMapper;

	@Override
	public GenericResponse<ForwardingRightsDTO> createForwardingRight(ForwardingRightsDTO dto) {
		ForwardingRights entity = forwardingRightsMapper.mapToEntity(dto);
		ForwardingRights savedEntity = repository.save(entity);
		return new GenericResponse<>(true, "Forwarding rights created successfully",
				forwardingRightsMapper.mapToDTO(savedEntity), HttpStatus.CREATED.value());
	}

	@Override
	public GenericResponse<ForwardingRightsDTO> updateForwardingRight(Long id, ForwardingRightsDTO dto) {
		ForwardingRights entity = repository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Forwarding rights not found with id: " + id));

		// Update entity fields
		entity.setApplicationId(dto.getApplicationId());
		entity.setSenderRoleId(dto.getSenderRoleId());
		entity.setReceiverRoleId(dto.getReceiverRoleId());
		entity.setLevel(dto.getLevel());

		ForwardingRights updatedEntity = repository.save(entity);
		return new GenericResponse<>(true, "Forwarding rights updated successfully",
				forwardingRightsMapper.mapToDTO(updatedEntity), HttpStatus.OK.value());
	}

	@Override
	public GenericResponse<ForwardingRightsDTO> getForwardingRightById(Long id) {
		ForwardingRights entity = repository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Forwarding rights not found with id: " + id));

		return new GenericResponse<>(true, "Forwarding rights fetched successfully",
				forwardingRightsMapper.mapToDTO(entity), HttpStatus.OK.value());
	}

	@Override
	public GenericResponse<List<ForwardingRightsDTO>> getAllForwardingRights() {
		List<ForwardingRights> entities = repository.findAll();
		List<ForwardingRightsDTO> dtos = entities.stream().map(forwardingRightsMapper::mapToDTO)
				.collect(Collectors.toList());
		return new GenericResponse<>(true, "Forwarding rights list fetched successfully", dtos, HttpStatus.OK.value());
	}

	@Override
	public GenericResponse<String> deleteForwardingRight(Long id) {
		ForwardingRights entity = repository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Forwarding rights not found with id: " + id));

		repository.delete(entity);
		return new GenericResponse<>(true, "Forwarding rights deleted successfully", "Deleted successfully",
				HttpStatus.OK.value());
	}

	/**
	 * Fetches user details based on applicationId and senderRoleId.
	 */
	public List<Map<String, Object>> getUserDetails(Long applicationId, Long senderRoleId) {
		ForwardingRights forwardingRights = repository.findByApplicationIdAndSenderRoleId(applicationId, senderRoleId)
				.orElseThrow(() -> new UserNotFoundException("ForwardingRights not found"));

		Integer roleId = forwardingRights.getSenderRoleId();

		List<Object[]> resultList = repository.findUserDetailsByRoleId(roleId);
		if (resultList.isEmpty()) {
			throw new UserNotFoundException("No user details found for the given role");
		}

		List<Map<String, Object>> usersDetailsList = new ArrayList<>();
		for (Object[] userResult : resultList) {
			Map<String, Object> userDetails = new HashMap<>();
			userDetails.put("firstName", userResult[0]);
			userDetails.put("middleName", userResult[1]);
			userDetails.put("lastName", userResult[2]);
			userDetails.put("email", userResult[3]);
			userDetails.put("officeId", userResult[4]);
			userDetails.put("officeName", userResult[5]);
			userDetails.put("roleName", userResult[6]);

			usersDetailsList.add(userDetails);
		}
		return usersDetailsList;
	}
}
