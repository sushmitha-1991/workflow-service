package com.dsp.workflow.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dsp.workflow.dto.PublishHistoryDTO;
import com.dsp.workflow.dto.UnPublishRequest;
import com.dsp.workflow.dto.UrlDto;
import com.dsp.workflow.service.PublishService;

@RestController
@RequestMapping("/workflow/publish-history")
public class PublishController {

    @Autowired
    private PublishService publishService;

    @PostMapping("/create")
    public PublishHistoryDTO save(@RequestBody PublishHistoryDTO dto) {
        System.out.println("Received DTO in Controller: " + dto);
        if (dto == null) {
            throw new RuntimeException("DTO is NULL. JSON payload might be incorrect.");
        }
        PublishHistoryDTO responseDto = publishService.save(dto);
        return responseDto;
    }

    @PostMapping("/getAll/publishedDocs")
    public ResponseEntity<List<UrlDto>> getByDate(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String applicationName,
            @RequestParam(required = false) String locale) {
        List<UrlDto> responseDto = publishService.getByDate(date, applicationName, locale);
        if (responseDto.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/update/{id}")
    public PublishHistoryDTO update(@PathVariable Long id, @RequestBody PublishHistoryDTO dto) {
        System.out.println("Id need to be unpublished" + id);
        if (id == null) {
            throw new RuntimeException("id is NULL");
        }
        PublishHistoryDTO responseDto = publishService.update(id, dto);
        return responseDto;
    }

    @PutMapping("/unpublish/{id}")
    public PublishHistoryDTO unpublishById(@PathVariable Long id, @RequestBody UnPublishRequest request) {
        System.out.println("Id need to be unpublished " + id);
        if (id == null) {
            throw new RuntimeException("id is NULL");
        }
        PublishHistoryDTO responseDto = publishService.updateById(id, request);
        return responseDto;
    }

    @GetMapping("/getByApplicationNo/{applicationNo}")
    public PublishHistoryDTO getbyAppNo(@PathVariable String applicationNo) {
        System.out.println("Id need to be unpublished" + applicationNo);
        if (applicationNo == null) {
            throw new RuntimeException("ApplicationNo is NULL.");
        }
        PublishHistoryDTO response = publishService.getbyAppNo(applicationNo);
        return response;
    }

    @PostMapping("/getAll/unpublishedDocs")
    public ResponseEntity<List<UrlDto>> getAllUnpublished(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String applicationName,
            @RequestParam(required = false) String locale) {
        List<UrlDto> responseDto = publishService.getAllUnpublished(date, applicationName, locale);
        if (responseDto.isEmpty()) {
            return ResponseEntity.noContent().build(); // Returns 204 No Content if the list is empty
        }
        return ResponseEntity.ok(responseDto); // Returns 200 OK with the list
    }

    @PostMapping("/getAll/docs")
    public List<UrlDto> getAllDocs(@RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String applicationName,
            @RequestParam(required = false) String locale) {
        List<UrlDto> responseDto = publishService.getAllDocs(date, applicationName, locale);
        return responseDto;
    }
}
