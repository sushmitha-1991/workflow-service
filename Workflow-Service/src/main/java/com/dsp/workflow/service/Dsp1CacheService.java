/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.dsp.workflow.dto.Dsp1CacheRegistryProjection;
import com.dsp.workflow.dto.PublishHistoryProjection;
import com.dsp.workflow.repository.ApplicationInstanceRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class Dsp1CacheService {

	private final ApplicationInstanceRepository applicationInstanceRepository;

	private final RestTemplate asyncRestTemplate;

	@Value("${dsp1.cache.clear.url}")
	private String dsp1CacheClearUrl;

	private static String DSP1_MONTHLY_CALENDAR_CACHE_NAME = "twelveHourCache";

	private static String DSP1_MONTHLY_CALENDAR_ENDPOINT = "/api_ls/ppHome/MonthlyCalendar";

	public static String TRP = "TRP";

	public Dsp1CacheService(RestTemplate asyncRestTemplate,
			ApplicationInstanceRepository applicationInstanceRepository) {
		this.asyncRestTemplate = asyncRestTemplate;
		this.applicationInstanceRepository = applicationInstanceRepository;
	}

	@Async
	public void clearCacheDsp1(String appNo, String appAbbr) {
		try {

			if (appAbbr.equals(TRP)) {
				appNo = appNo + "_HI";
			}

			PublishHistoryProjection docDetails = applicationInstanceRepository.getAppDetailsByAbbr(appNo);

			List<Dsp1CacheRegistryProjection> dsp1CacheList = applicationInstanceRepository
					.getDsp1CacheDataList(docDetails.getApplicationId());

			if (appAbbr.equalsIgnoreCase("PC")) {
				Dsp1CacheRegistryProjection dsp1Cache = dsp1CacheList.stream()
						.filter(cache -> "PC".equalsIgnoreCase(cache.getApplicationAbbr())).findFirst().orElse(null);

				if (dsp1Cache != null) {
					String endpoint = dsp1Cache.getDsp1Endpoint()
							.replaceFirst("\\$", docDetails.getLokSabhaNo() != null ? docDetails.getLokSabhaNo() : "")
							.replaceFirst("\\$",
									docDetails.getLokSabhaSessionNo() != null ? docDetails.getLokSabhaSessionNo() : "")
							.replaceFirst("\\$", docDetails.getLocale() != null ? docDetails.getLocale() : "");

					String redisKey = dsp1Cache.getDsp1CacheName() + "::" + endpoint;
					clearCacheFromDSP1(redisKey);
				}

			} else if (appAbbr.equalsIgnoreCase("B2")) {

				for (Dsp1CacheRegistryProjection dsp1Cache : dsp1CacheList) {

					String endpoint = dsp1Cache.getDsp1Endpoint();

					if (endpoint.contains("bulletin2Url")) {

						String urlEndpoint = endpoint.replaceFirst("\\$", docDetails.getDay().toString())
								.replaceFirst("\\$", docDetails.getMonth().toString())
								.replaceFirst("\\$", docDetails.getYear().toString())
								.replaceFirst("\\$", docDetails.getLocale());

						clearCacheFromDSP1(dsp1Cache.getDsp1CacheName() + "::" + urlEndpoint);
					}

					if (endpoint.contains("bulletin2Dates")) {

						String dateEndpoint = endpoint.replaceFirst("\\$", docDetails.getMonth().toString())
								.replaceFirst("\\$", docDetails.getYear().toString());

						clearCacheFromDSP1(dsp1Cache.getDsp1CacheName() + "::" + dateEndpoint);
					}
				}
			}

			String monthlyCalendarEndpoint = DSP1_MONTHLY_CALENDAR_ENDPOINT + "?month=" + docDetails.getMonth()
					+ "&year=" + docDetails.getYear() + "&locale=" + docDetails.getLocale();

			String monthlyCalendarRedisKey = DSP1_MONTHLY_CALENDAR_CACHE_NAME + "::" + monthlyCalendarEndpoint;
//			System.out.println(">>> monthlyCalendarRedisKey " + monthlyCalendarRedisKey);
			clearCacheFromDSP1(monthlyCalendarRedisKey);

		} catch (Exception e) {
			log.error("❌ Error in async DSP1 cache clearing for appNo={}, appAbbr={}: {}", appNo, appAbbr,
					e.getMessage());
		}
	}

	private void clearCacheFromDSP1(String redisKey) {
		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("redisKey", redisKey);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

		try {
			ResponseEntity<String> response = asyncRestTemplate.exchange(dsp1CacheClearUrl, HttpMethod.DELETE, entity,
					String.class);
//			System.out.println("response " + response.getBody());

			if (response.getStatusCode().is2xxSuccessful()) {
				log.info("✅ Cache cleared successfully for key: {}", redisKey);
			} else {
				log.warn("⚠️ Failed to clear cache for key: {}, status: {}", redisKey, response.getStatusCode());
			}

		} catch (Exception e) {
			log.error("❌ Error while clearing DSP1 cache for key {}: {}", redisKey, e.getMessage());
		}
	}
}
