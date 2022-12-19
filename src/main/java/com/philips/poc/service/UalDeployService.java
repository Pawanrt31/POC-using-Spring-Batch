package com.philips.poc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.philips.poc.model.UalDeployRequest;

public interface UalDeployService {

	//void createEntryInDBAndDropMessage(String packageId, String ruleName, String distributionRefId) throws JsonProcessingException;
	
	void createEntryInDBAndDropMessage(UalDeployRequest ualDeployRequest) throws JsonProcessingException;
}
