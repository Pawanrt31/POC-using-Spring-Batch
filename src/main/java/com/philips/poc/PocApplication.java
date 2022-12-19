package com.philips.poc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class PocApplication {

	public static void main(String[] args) {
		SpringApplication.run(PocApplication.class, args);
	}

	@PostConstruct
	public void startupApplication() {
		log.info("::::::::::::::Started POC Service::::::::::::::");
	}

	@PreDestroy
	public void shutdownApplication() {
		log.info("::::::::::::::POC Stopped::::::::::::::");
	}

	@Bean
	@Profile("default")
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

	@Bean
	@Profile("cloud")
	public RestTemplate getRestTemplateCloud() {
		return new RestTemplate();
	}
}
