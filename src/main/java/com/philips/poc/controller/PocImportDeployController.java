package com.philips.poc.controller;

import com.philips.poc.constant.Constants;
import com.philips.poc.error.FirmwareManagementException;
import com.philips.poc.model.RuleRequest;
import com.philips.poc.model.StatusUpdateRequest;
import com.philips.poc.model.UalDeployRequest;
import com.philips.poc.model.UalDetailsResponse;
import com.philips.poc.service.UalDeployService;
import com.philips.poc.service.UalImportService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

import javax.validation.Valid;

@RestController
@RequestMapping("poc/")
@Validated
public class PocImportDeployController {

	@Autowired
	private UalImportService ualImportService;
	
	@Autowired
	private UalDeployService ualDeployService;

	@PostMapping(value = "$import", produces = "application/json", consumes = "multipart/form-data", headers = Constants.UAL_IMPORT_HEADER)
	public void importUalPoc(@RequestParam("file") MultipartFile multipartFile,
			@Valid @RequestParam(value = "ruleName", required = true) String ruleName,
			@Valid @RequestParam(value = "business", required = true) String business,
			@Valid @RequestParam(value = "productType", required = true) String productType,
			@Valid @RequestParam(value = "ruleDescription", required = true) String ruleDescription,
			@Valid @RequestParam(value = "ruleExpression", required = true) String ruleExpression,
			@RequestHeader(value = "Entity-User", required = false) String entityUser) throws Exception {
		RuleRequest request = new RuleRequest();
		request.setRuleName(ruleName);
		request.setBusiness(business);
		request.setProductType(productType);
		request.setRuleDescription(ruleDescription);
		request.setRuleExpression(ruleExpression);
		request.setUalTemplate(true);
		ualImportService.ualImport(multipartFile, request, entityUser);

	}

	@PostMapping(value = "$importPagedApi", consumes = "application/json", headers = Constants.UAL_IMPORT_HEADER)
	public UalDetailsResponse importPagedApi(@Valid @RequestBody RuleRequest ruleRequest,
			@RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo) throws Exception {
		return ualImportService.getPagedData(ruleRequest, pageNo);
	}

	@PostMapping(value = "$importWithoutFile", consumes = "application/json", headers = Constants.UAL_IMPORT_HEADER)
	public ResponseEntity<Void> importUalPocWithoutFile(@Valid @RequestBody RuleRequest ruleRequest)
			throws FirmwareManagementException {
		System.out.println("Executing $importWithoutFile");
		CompletableFuture.runAsync(() -> {
			try {
				ualImportService.populateDB(ruleRequest, ruleRequest.getSize());
			} catch (FirmwareManagementException e) {
				System.out.println(e.getErrors().getErrorMessage());
			}
		});
		System.out.println("Completed API");
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping(value = "$importualstatus", consumes = "application/json", headers = Constants.UAL_IMPORT_HEADER)
	public ResponseEntity<Void> statusOfProcessing(@Valid @RequestBody StatusUpdateRequest updateRequest)
			throws Exception {
		ualImportService.saveToImportTracker(updateRequest);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@PostMapping(value = "$deploy", produces = "application/json", consumes = "application/json", headers = Constants.UAL_IMPORT_HEADER)
	public ResponseEntity<Void> deployPoc(
			//@RequestParam(value = "packageId", required = true) String packageId,
			//@RequestParam(value = "ruleName", required = true) String ruleName,@RequestParam(value = "distributionRefId", required = true) String distributionRefId,
			@Valid @RequestBody UalDeployRequest ualDeployRequest,
			@RequestHeader(value = "Entity-User", required = false) String entityUser) {
		System.out.println("Executing $deploy");
		CompletableFuture.runAsync(() -> {
		
			try {
				//ualDeployService.createEntryInDBAndDropMessage(packageId, ruleName, distributionRefId);
				ualDeployService.createEntryInDBAndDropMessage(ualDeployRequest);
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		});
		return new ResponseEntity<>(HttpStatus.OK);

	}
}
