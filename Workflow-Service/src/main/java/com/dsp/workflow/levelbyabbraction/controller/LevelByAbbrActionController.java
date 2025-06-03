package com.dsp.workflow.levelbyabbraction.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dsp.workflow.levelbyabbraction.dto.LevelByAbbrActionDTO;
import com.dsp.workflow.levelbyabbraction.dto.LevelByAbbrActionSearchDTO;
import com.dsp.workflow.levelbyabbraction.service.LevelByAbbrActionService;
import com.dsp.workflow.response.GenericResponse;
import com.dsp.workflow.util.Utils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/levelabbr")
public class LevelByAbbrActionController {

	@Autowired
	private LevelByAbbrActionService levelByAbbrActionService;

	@Autowired
	private Utils utils;

	@PostMapping("/create")
	@Operation(summary = "Create a new LevelByAbbrAction entry")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "LevelByAbbrAction created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input data"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<GenericResponse<LevelByAbbrActionDTO>> createLevelByAbbrAction(
			@Valid @RequestBody LevelByAbbrActionDTO levelByAbbrActionDTO) {
		LevelByAbbrActionDTO createdLevelByAbbrAction = levelByAbbrActionService
				.createLevelByAbbrAction(levelByAbbrActionDTO);
		GenericResponse<LevelByAbbrActionDTO> response = new GenericResponse<>(true,
				"LevelByAbbrAction created successfully", createdLevelByAbbrAction, HttpStatus.CREATED.value());
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get LevelByAbbrAction by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "LevelByAbbrAction retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "LevelByAbbrAction not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<GenericResponse<LevelByAbbrActionDTO>> getLevelByAbbrActionById(@PathVariable("id") Long id) {
		LevelByAbbrActionDTO levelByAbbrActionDTO = levelByAbbrActionService.getLevelByAbbrActionById(id);
		GenericResponse<LevelByAbbrActionDTO> response = new GenericResponse<>(true,
				"LevelByAbbrAction retrieved successfully", levelByAbbrActionDTO, HttpStatus.OK.value());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/getAll")
	@Operation(summary = "Get all LevelByAbbrActions")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "LevelByAbbrActions retrieved successfully"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<GenericResponse<List<LevelByAbbrActionDTO>>> getAllLevelByAbbrActions() {
		List<LevelByAbbrActionDTO> levelByAbbrActionDTOs = levelByAbbrActionService.getAllLevelByAbbrActions();
		GenericResponse<List<LevelByAbbrActionDTO>> response = new GenericResponse<>(true,
				"LevelByAbbrActions retrieved successfully", levelByAbbrActionDTOs, HttpStatus.OK.value());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/getActionsbyApplicationID")
	@Operation(summary = "Search LevelByAbbrActions by level, applicationId, and roleIds")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "LevelByAbbrActions retrieved successfully"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<GenericResponse<List<LevelByAbbrActionDTO>>> searchLevelByAbbrActions(
			@RequestBody LevelByAbbrActionSearchDTO searchDTO) {
		List<Long> roleIds = utils.getRoleIdsByUserId(); // Assuming userService has this method
		List<LevelByAbbrActionDTO> levelByAbbrActions = levelByAbbrActionService
				.getLevelByAbbrActionsByCriteria(searchDTO.getLevel(), searchDTO.getApplicationId(), roleIds);
		String message = searchDTO.getApplicationId() + ".LevelByAbbrActions";
		GenericResponse<List<LevelByAbbrActionDTO>> response = new GenericResponse<>(true, message, levelByAbbrActions,
				HttpStatus.OK.value());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update an existing LevelByAbbrAction")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "LevelByAbbrAction updated successfully"),
			@ApiResponse(responseCode = "404", description = "LevelByAbbrAction not found"),
			@ApiResponse(responseCode = "400", description = "Invalid input data"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<GenericResponse<LevelByAbbrActionDTO>> updateLevelByAbbrAction(@PathVariable("id") Long id,
			@Valid @RequestBody LevelByAbbrActionDTO levelByAbbrActionDTO) {
		LevelByAbbrActionDTO updatedLevelByAbbrAction = levelByAbbrActionService.updateLevelByAbbrAction(id,
				levelByAbbrActionDTO);
		GenericResponse<LevelByAbbrActionDTO> response = new GenericResponse<>(true,
				"LevelByAbbrAction updated successfully", updatedLevelByAbbrAction, HttpStatus.OK.value());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a LevelByAbbrAction by ID")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "LevelByAbbrAction deleted successfully"),
			@ApiResponse(responseCode = "404", description = "LevelByAbbrAction not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<GenericResponse<Void>> deleteLevelByAbbrAction(@PathVariable("id") Long id) {
		levelByAbbrActionService.deleteLevelByAbbrAction(id);
		GenericResponse<Void> response = new GenericResponse<>(true, "LevelByAbbrAction deleted successfully", null,
				HttpStatus.OK.value());
		return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
	}

}
