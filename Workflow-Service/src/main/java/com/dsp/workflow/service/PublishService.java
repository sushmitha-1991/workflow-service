package com.dsp.workflow.service;

import java.time.LocalDate;
import java.util.List;

import com.dsp.workflow.dto.PublishHistoryDTO;
import com.dsp.workflow.dto.UnPublishRequest;
import com.dsp.workflow.dto.UrlDto;

public interface PublishService {

    PublishHistoryDTO save(PublishHistoryDTO dto);

    List<UrlDto> getByDate(LocalDate date, String documentName, String locale);
    UrlDto getByDateDoc(LocalDate date, String documentName, String locale);

    PublishHistoryDTO update(Long id,PublishHistoryDTO dto);


    PublishHistoryDTO updateById(Long id,UnPublishRequest request);

    PublishHistoryDTO getbyAppNo(String applicationNo);

    UrlDto getAllUnpublishedDoc(LocalDate date, String applicationName, String locale);

    List<UrlDto> getAllUnpublished(LocalDate date, String applicationName, String locale);

    List<UrlDto> getAllDocs(LocalDate date, String applicationName, String locale);

    List<UrlDto> getAll();

    UrlDto getBySession(LocalDate forDate,String lsNo,String sessNo,
    String rsNo, String applicationName, String locale);


}






