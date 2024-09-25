/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.forwardingrights.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dsp.workflow.forwardingrights.dto.ForwardingRightsDTO;
import com.dsp.workflow.forwardingrights.service.ForwardingRightsService;
import com.dsp.workflow.response.GenericResponse;
import com.dsp.workflow.util.Utils;

/**
 * 
 */
@RestController
@RequestMapping("/workflow/forwarding-rights")
public class ForwardingRightsController {

	@Autowired
	private ForwardingRightsService forwardingRightsService;

	@Autowired
	private Utils utils;

	@PostMapping("/create")
	public ResponseEntity<GenericResponse<ForwardingRightsDTO>> createForwardingRight(
			@RequestBody ForwardingRightsDTO dto) {
		GenericResponse<ForwardingRightsDTO> response = forwardingRightsService.createForwardingRight(dto);
		return ResponseEntity.status(response.getStatusCode()).body(response);
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<GenericResponse<ForwardingRightsDTO>> updateForwardingRight(@PathVariable("id") Long id,
			@RequestBody ForwardingRightsDTO dto) {
		GenericResponse<ForwardingRightsDTO> response = forwardingRightsService.updateForwardingRight(id, dto);
		return ResponseEntity.status(response.getStatusCode()).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<GenericResponse<ForwardingRightsDTO>> getForwardingRightById(@PathVariable("id") Long id) {
		GenericResponse<ForwardingRightsDTO> response = forwardingRightsService.getForwardingRightById(id);
		return ResponseEntity.status(response.getStatusCode()).body(response);
	}

	@GetMapping("/getAll")
	public ResponseEntity<GenericResponse<List<ForwardingRightsDTO>>> getAllForwardingRights() {
		GenericResponse<List<ForwardingRightsDTO>> response = forwardingRightsService.getAllForwardingRights();
		return ResponseEntity.status(response.getStatusCode()).body(response);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<GenericResponse<String>> deleteForwardingRight(@PathVariable("id") Long id) {
		GenericResponse<String> response = forwardingRightsService.deleteForwardingRight(id);
		return ResponseEntity.status(response.getStatusCode()).body(response);
	}

	@GetMapping("/user-details")
	public ResponseEntity<List<Map<String, Object>>> getUserDetails(
			@RequestParam(name = "applicationId") Long applicationId) {
		// Fetch the sender's role ID using the utility function
		Long senderRoleId = utils.getRoleIdByUserId();
		System.out.println("Sender Role ID: " + senderRoleId);

		// Call the service to get the list of user details
		List<Map<String, Object>> userDetails = (List<Map<String, Object>>) forwardingRightsService
				.getUserDetails(applicationId, senderRoleId);

		// Return the user details as the response
		return ResponseEntity.ok(userDetails);
	}

}