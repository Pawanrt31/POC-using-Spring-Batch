package com.philips.poc.service;

import com.philips.poc.error.FirmwareManagementException;
import com.philips.poc.model.RuleRequest;
import com.philips.poc.model.StatusUpdateRequest;
import com.philips.poc.model.UalDetailsResponse;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import javax.validation.Valid;

public interface UalImportService {

	void ualImport(MultipartFile multiPartFile, RuleRequest request, String entityUser) throws FirmwareManagementException, IOException, Exception;
	
	UalDetailsResponse getPagedData(RuleRequest ruleRequest, int pageNo);
	
	void populateDB(RuleRequest ruleRequest, int totalRecords) throws FirmwareManagementException;
	
	void saveToImportTracker(@Valid StatusUpdateRequest updateRequest);
}
