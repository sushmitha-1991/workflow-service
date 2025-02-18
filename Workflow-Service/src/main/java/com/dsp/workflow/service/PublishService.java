package com.dsp.workflow.service;

import java.time.LocalDate;

import com.dsp.workflow.dto.PublishHistoryDTO;
import com.dsp.workflow.dto.UnPublishRequest;
import com.dsp.workflow.dto.UrlDto;

public interface PublishService {

    PublishHistoryDTO save(PublishHistoryDTO dto);

    UrlDto getByDate(LocalDate date, String documentName, String locale);
    PublishHistoryDTO update(Long id,PublishHistoryDTO dto);


    PublishHistoryDTO updateById(Long id,UnPublishRequest request);

    PublishHistoryDTO getbyAppNo(String applicationNo);

    UrlDto getAllUnpublished(LocalDate date, String applicationName, String locale);
}
