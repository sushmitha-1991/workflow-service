/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 
 */
@Configuration
public class AsyncRestTemplateConfig {

	@Bean
	@Qualifier("asyncRestTemplate")
	public RestTemplate asyncRestTemplate() {
		return new RestTemplate();
	}
}
