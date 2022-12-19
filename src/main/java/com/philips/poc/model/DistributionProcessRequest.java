package com.philips.poc.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DistributionProcessRequest {

	private String resourceType;
	private String distributionRefId;
	private Object payload;
	private String type;
	private String distributionType;

}