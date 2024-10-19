package com.dsp.workflow.levelbyabbraction.controller;


import com.dsp.workflow.levelbyabbraction.dto.LevelByAbbrActionDTO;
import com.dsp.workflow.levelbyabbraction.service.LevelByAbbrActionService;
import com.dsp.workflow.response.GenericResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/levelabbr")
public class LevelByAbbrActionController {


    @Autowired
    private LevelByAbbrActionService levelByAbbrActionService;

    @PostMapping("/create")
    @Operation(summary = "Create a new LevelByAbbrAction entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "LevelByAbbrAction created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<GenericResponse<LevelByAbbrActionDTO>> createLevelByAbbrAction(@Valid @RequestBody LevelByAbbrActionDTO levelByAbbrActionDTO) {
        LevelByAbbrActionDTO createdLevelByAbbrAction = levelByAbbrActionService.createLevelByAbbrAction(levelByAbbrActionDTO);
        GenericResponse<LevelByAbbrActionDTO> response = new GenericResponse<>(
                true,
                "LevelByAbbrAction created successfully",
                createdLevelByAbbrAction,
                HttpStatus.CREATED.value()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get LevelByAbbrAction by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "LevelByAbbrAction retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "LevelByAbbrAction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<GenericResponse<LevelByAbbrActionDTO>> getLevelByAbbrActionById(@PathVariable("id") Long id) {
        LevelByAbbrActionDTO levelByAbbrActionDTO = levelByAbbrActionService.getLevelByAbbrActionById(id);
        GenericResponse<LevelByAbbrActionDTO> response = new GenericResponse<>(
                true,
                "LevelByAbbrAction retrieved successfully",
                levelByAbbrActionDTO,
                HttpStatus.OK.value()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getAll")
    @Operation(summary = "Get all LevelByAbbrActions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "LevelByAbbrActions retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<GenericResponse<List<LevelByAbbrActionDTO>>> getAllLevelByAbbrActions() {
        List<LevelByAbbrActionDTO> levelByAbbrActionDTOs = levelByAbbrActionService.getAllLevelByAbbrActions();
        GenericResponse<List<LevelByAbbrActionDTO>> response = new GenericResponse<>(
                true,
                "LevelByAbbrActions retrieved successfully",
                levelByAbbrActionDTOs,
                HttpStatus.OK.value()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/search")
    @Operation(summary = "Search LevelByAbbrActions by level, applicationId, and roleId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "LevelByAbbrActions retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<GenericResponse<List<LevelByAbbrActionDTO>>> searchLevelByAbbrActions(
            @RequestParam String level,
            @RequestParam Long applicationId,
            @RequestParam Integer roleId) {
        List<LevelByAbbrActionDTO> levelByAbbrActions = levelByAbbrActionService.getLevelByAbbrActionsByLevelAndApplicationIdAndRoleId(level, applicationId, roleId);
        GenericResponse<List<LevelByAbbrActionDTO>> response = new GenericResponse<>(
                true,
                "LevelByAbbrActions retrieved successfully",
                levelByAbbrActions,
                HttpStatus.OK.value()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing LevelByAbbrAction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "LevelByAbbrAction updated successfully"),
            @ApiResponse(responseCode = "404", description = "LevelByAbbrAction not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<GenericResponse<LevelByAbbrActionDTO>> updateLevelByAbbrAction(@PathVariable("id") Long id, @Valid @RequestBody LevelByAbbrActionDTO levelByAbbrActionDTO) {
        LevelByAbbrActionDTO updatedLevelByAbbrAction = levelByAbbrActionService.updateLevelByAbbrAction(id, levelByAbbrActionDTO);
        GenericResponse<LevelByAbbrActionDTO> response = new GenericResponse<>(
                true,
                "LevelByAbbrAction updated successfully",
                updatedLevelByAbbrAction,
                HttpStatus.OK.value()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a LevelByAbbrAction by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "LevelByAbbrAction deleted successfully"),
            @ApiResponse(responseCode = "404", description = "LevelByAbbrAction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<GenericResponse<Void>> deleteLevelByAbbrAction(@PathVariable("id") Long id) {
        levelByAbbrActionService.deleteLevelByAbbrAction(id);
        GenericResponse<Void> response = new GenericResponse<>(
                true,
                "LevelByAbbrAction deleted successfully",
                null,
                HttpStatus.OK.value()
        );
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

}
