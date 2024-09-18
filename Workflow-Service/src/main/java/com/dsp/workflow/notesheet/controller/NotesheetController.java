package com.dsp.workflow.notesheet.controller;

import com.dsp.workflow.notesheet.dto.NotesheetDTO;
import com.dsp.workflow.notesheet.service.NotesheetService;
import com.dsp.workflow.response.GenericResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notesheet")
@Tag(name = "Notesheet", description = "Operations related to notesheet management")
public class NotesheetController {

    @Autowired
    private NotesheetService notesheetService;

    @PostMapping("/create")
    @Operation(summary = "Create a new notesheet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notesheet created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<GenericResponse<NotesheetDTO>> createNotesheet(@Valid @RequestBody NotesheetDTO notesheetDTO) {
        NotesheetDTO createdNotesheet = notesheetService.createNotesheet(notesheetDTO);
        GenericResponse<NotesheetDTO> response = new GenericResponse<>(
                true,
                "Notesheet created successfully",
                createdNotesheet,
                HttpStatus.CREATED.value()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notesheet by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notesheet fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Notesheet not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<GenericResponse<NotesheetDTO>> getNotesheetById(@PathVariable ("id") Long id) {
        NotesheetDTO notesheetDTO = notesheetService.getNotesheetById(id);
        GenericResponse<NotesheetDTO> response = new GenericResponse<>(
                true,
                "Notesheet fetched successfully",
                notesheetDTO,
                HttpStatus.OK.value()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getAll")
    @Operation(summary = "Get all notesheets")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notesheets fetched successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<GenericResponse<List<NotesheetDTO>>> getAllNotesheets() {
        List<NotesheetDTO> notesheets = notesheetService.getAllNotesheets();
        GenericResponse<List<NotesheetDTO>> response = new GenericResponse<>(
                true,
                "Notesheets fetched successfully",
                notesheets,
                HttpStatus.OK.value()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "Update an existing notesheet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notesheet updated successfully"),
            @ApiResponse(responseCode = "404", description = "Notesheet not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<GenericResponse<NotesheetDTO>> updateNotesheet(@PathVariable ("id") Long id, @RequestBody NotesheetDTO notesheetDTO) {
        NotesheetDTO updatedNotesheet = notesheetService.updateNotesheet(id, notesheetDTO);
        GenericResponse<NotesheetDTO> response = new GenericResponse<>(
                true,
                "Notesheet updated successfully",
                updatedNotesheet,
                HttpStatus.OK.value()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notesheet by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notesheet deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Notesheet not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<GenericResponse<Void>> deleteNotesheet(@PathVariable("id") Long id) {
        notesheetService.deleteNotesheet(id);
        GenericResponse<Void> response = new GenericResponse<>(
                true,
                "Notesheet deleted successfully",
                null,
                HttpStatus.NO_CONTENT.value()
        );
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    @GetMapping("/history/{historyId}")
    @Operation(summary = "Get notesheet history by history ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "History fetched successfully"),
            @ApiResponse(responseCode = "404", description = "History not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<GenericResponse<List<NotesheetDTO>>> getNotesheetHistory(@PathVariable("historyId") int historyId) {
        List<NotesheetDTO> history = notesheetService.getNotesheetHistory(historyId);
        GenericResponse<List<NotesheetDTO>> response = new GenericResponse<>(
                true,
                "History fetched successfully",
                history,
                HttpStatus.OK.value()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}


