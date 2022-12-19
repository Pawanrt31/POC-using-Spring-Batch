package com.philips.poc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties
@Getter
@Setter
public class PocConfig {

	private String pocUrl;
	private String region;
	private String distributionQueue;
	private String distributionExchange;
	private String distributionRoutingKey;
	private String deployExchange;
	private String deployRoutingKey;
	private String deployQueue;
}
