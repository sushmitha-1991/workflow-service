package com.dsp.workflow.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
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
    public UrlDto getByDate(@RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String applicationName,
            @RequestParam(required = false) String locale) {
        UrlDto responseDto = publishService.getByDate(date, applicationName, locale);
        return responseDto;
    }
    @PutMapping("/update/{id}")
    public PublishHistoryDTO update(@PathVariable Long id,@RequestBody PublishHistoryDTO dto) {
        System.out.println("Id need to be unpublished"+id);
        if (id == null) {
            throw new RuntimeException("id is NULL");
        }
        PublishHistoryDTO responseDto = publishService.update(id,dto);
        return responseDto;
    }
    @PutMapping("/unpublish/{id}")
    public PublishHistoryDTO unpublishById(@PathVariable Long id, @RequestBody UnPublishRequest request) {
        System.out.println("Id need to be unpublished "+id);
        if (id == null) {
            throw new RuntimeException("id is NULL");
        }
        PublishHistoryDTO responseDto = publishService.updateById(id,request);
        return responseDto;
    }
    @GetMapping("/getByApplicationNo/{applicationNo}")
    public PublishHistoryDTO getbyAppNo(@PathVariable String applicationNo) {
        System.out.println("Id need to be unpublished"+applicationNo);
        if (applicationNo == null) {
            throw new RuntimeException("ApplicationNo is NULL.");
        }
        PublishHistoryDTO response = publishService.getbyAppNo(applicationNo);
        return response;
    }
    @PostMapping("/getAll/unpublishedDocs")
    public UrlDto getAllUnpublished(@RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String applicationName,
            @RequestParam(required = false) String locale) {
        UrlDto responseDto = publishService.getAllUnpublished(date, applicationName, locale);
        return responseDto;
    }
}
