package com.philips.poc.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UalDeployRequest {

	private String packageId;
	private String ruleName;
	private String distributionRefId;
	private String productType;
}
