package com.dsp.workflow.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.dsp.workflow.dto.PublishHistoryDTO;
import com.dsp.workflow.dto.PublishHistoryResponseDTO;
import com.dsp.workflow.dto.SessionDto;

import com.dsp.workflow.dto.ResponseDTO;

import com.dsp.workflow.dto.UnPublishRequest;
import com.dsp.workflow.dto.UrlDto;

public interface PublishService {

    PublishHistoryDTO save(PublishHistoryDTO dto);

    List<UrlDto> getPublishedDocsFromDSP1(LocalDate date, String documentName,
     String locale, String lsNo, String sesNo, String rssNo,String house);

    UrlDto getByDateDoc(LocalDate date, String documentName, String locale);

    PublishHistoryDTO update(Long id, PublishHistoryDTO dto);

    PublishHistoryDTO updateById(Long id, UnPublishRequest request);

    PublishHistoryDTO getbyAppNo(String applicationNo);

    UrlDto getAllUnpublishedDoc(LocalDate date, String applicationName, String locale);

    List<UrlDto> getAllUnpublished(LocalDate date, String applicationName, String locale);

    List<UrlDto> getAllDocs(LocalDate date, String applicationName, String locale);

    List<UrlDto> getAll();

    UrlDto getBySession(LocalDate forDate, String lsNo, String sessNo,
            String rsNo, String applicationName, String locale);

    List<String> uploadToDsp2(String applicationName, SessionDto dto);

    List<PublishHistoryDTO> getPublishedDocs(LocalDate date, String applicationName, String locale);

    List<ResponseDTO> getDocsByAppId(Long appId, String abbr, String lsNo, String sesNo, LocalDate fromDate,
            LocalDate toDate);

    
    String createDmsFolderStructure(String root, List<String> folders);

	List<PublishHistoryResponseDTO> getFilteredPublishHistory(Long applicationId, String lokSabhaNo,
			String lokSabhaSessionNo, String rajyaSabhaSessionNo, Long officeId, LocalDateTime publishDateFrom,
			LocalDateTime publishDateTo, LocalDate documentDateFrom, LocalDate documentDateTo);

}
