package com.philips.poc.service.impl;

import com.philips.poc.config.PocConfig;
import com.philips.poc.constant.Constants;
import com.philips.poc.entity.DeviceType;
import com.philips.poc.entity.DistributionRule;
import com.philips.poc.entity.ImportTracker;
import com.philips.poc.entity.SyncTracker;
import com.philips.poc.entity.UalDetails;
import com.philips.poc.error.FirmwareManagementException;
import com.philips.poc.error.FmsErrorCodes;
import com.philips.poc.model.RuleRequest;
import com.philips.poc.model.StatusUpdateRequest;
import com.philips.poc.model.UalDetailsResponse;
import com.philips.poc.repository.DeviceTypeRepository;
import com.philips.poc.repository.DistributionRuleRepository;
import com.philips.poc.repository.ImportTrackerRepository;
import com.philips.poc.repository.SyncTrackerRepository;
import com.philips.poc.repository.UalDetailsRepository;
import com.philips.poc.service.UalImportService;
import com.philips.poc.utils.HeaderUtils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UalImportServiceImpl implements UalImportService{
	
	@Autowired
	private PocConfig pocConfig;
	
	@Autowired
	private HeaderUtils headerUtils;

	@Autowired
	private DistributionRuleRepository distributionRuleRepository;

	@Autowired
	private DeviceTypeRepository deviceTypeRepository;

	@Autowired
	private UalDetailsRepository ualDetailsRepository;

	@Autowired
	private ImportTrackerRepository importTrackerRepository;

	@Autowired
	private SyncTrackerRepository syncTrackerRepository;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	JobLauncher jobLauncher;

	@Autowired
	Job processJob;

	@Autowired
	private Job job;

	@Autowired
	private SetMultipartFile setMultipartFile;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Override
	public void ualImport(MultipartFile multiPartFile, RuleRequest request, String entityUser) throws FirmwareManagementException, IOException, Exception {
		Boolean isValidFileFormat = false;
		XSSFWorkbook workbook = null;
		XSSFSheet worksheet = null;
		if (null != "xls,xlsx") {
			isValidFileFormat = "xls,xlsx".contains(multiPartFile.getOriginalFilename()
					.split("\\.")[multiPartFile.getOriginalFilename().split("\\.").length - 1]) ? true : false;
		}
		if (!isValidFileFormat) {
			log.error("Import failed,UAL template is not of correct file type");
			throw new FirmwareManagementException(FmsErrorCodes.INVALID_UAL_TEMPLATE_FILE_TYPE);
		}
		try {
			workbook = new XSSFWorkbook(multiPartFile.getInputStream());
			worksheet = workbook.getSheet("Input_UAL (transactions)");
		} catch (Exception e) {
			log.error("Import failed, UAL Template is invalid");
			throw new FirmwareManagementException(FmsErrorCodes.INAVLID_UAL_TEMPLATE);
		}
		if (worksheet == null) {
			log.error("Import failed, " + "Input_UAL (transactions)" + " sheet is not present");
			workbook.close();
			throw new FirmwareManagementException(FmsErrorCodes.INAVLID_UAL_TEMPLATE);

		}
		int column = -1;
		try {
			for (Cell cell : worksheet.getRow(0)) {
				if (cell.getRichStringCellValue().getString().trim().equals("As-Maintained System Equipment Number")) {
					column = cell.getColumnIndex();
					break;
				}
			}
		} catch (NullPointerException e) {
			log.error("Import failed, Header Is Not Provided in the template");
			workbook.close();
			throw new FirmwareManagementException(FmsErrorCodes.INAVLID_UAL_HEADER);
		}
		if (column == -1) {
			log.error("Import failed, " + "As-Maintained System Equipment Number" + " column is not present");
			workbook.close();
			throw new FirmwareManagementException(FmsErrorCodes.INAVLID_UAL_TEMPLATE);
		}
		log.info("Fetching rule expression for import UAL template");
		ruleExpressionList(multiPartFile, worksheet, column, request, entityUser);
		workbook.close();
	}

	private void ruleExpressionList(MultipartFile multipartFile, XSSFSheet worksheet, int column, RuleRequest request,
			String entityUser) throws FirmwareManagementException, Exception {
		int countEquipmentNumber = 0;
		int rowNumber = 1;
		List<String> equipmentNumberList = new ArrayList<>();
		log.info("Generating rule expression list for provided equipment numbers");
		while (countEquipmentNumber <= 50000 && rowNumber < 50001) {
			XSSFRow row = worksheet.getRow(rowNumber);
			try {
				if (row != null) {
					if (row.getCell(column) != null
							&& row.getCell(column).getCellType().name().toString().equals("STRING")) {
						String cellValue = row.getCell(column).getStringCellValue();
						if (!cellValue.matches(Constants.REGEX_PATTERN_ALPHANUMERIC_HYPHEN))
							throw new FirmwareManagementException(FmsErrorCodes.INAVLID_CONTENT_TYPE);
						equipmentNumberList.add(cellValue);
						countEquipmentNumber++;
					}
					if (row.getCell(column) != null
							&& row.getCell(column).getCellType().name().toString().equals("NUMERIC")) {
						Long cellValue = (long) row.getCell(column).getNumericCellValue();
						if (!cellValue.toString().matches(Constants.REGEX_PATTERN_ALPHANUMERIC_HYPHEN))
							throw new FirmwareManagementException(FmsErrorCodes.INAVLID_CONTENT_TYPE);
						equipmentNumberList.add(cellValue.toString());
						countEquipmentNumber++;
					}
				}
			} catch (Exception e) {
				throw new FirmwareManagementException(FmsErrorCodes.INAVLID_CONTENT_TYPE);
			}
			rowNumber++;
			if (countEquipmentNumber > 50000) {
				log.error("Exceeded records");
			}
		}
		setMultipartFile.setEquipmentNumbers(equipmentNumberList);
		launchJob(multipartFile, request, entityUser, equipmentNumberList);
	}

	private void launchJob(MultipartFile multiPartFile, RuleRequest request, String entityUser,
			List<String> equipmentNumbersList) throws Exception {
		setMultipartFile.setFile(multiPartFile);
		setMultipartFile.setRuleRequest(request);
		setMultipartFile.setEntityUser(entityUser);
		final DeviceType devicetype = deviceTypeRepository
				.findByDevicetypeNameOrDevicetypeDisplayname(request.getProductType());
		DistributionRule distributionRules = null;
		distributionRules = distributionRuleRepository.findRuleByRuleNameAndPropositionName(request.getRuleName(),
				request.getBusiness());
		if (distributionRules == null) {
			log.debug("Rule name " + request.getRuleName() + "Not Exists under Business " + request.getBusiness());
			distributionRules = new DistributionRule();
			distributionRules.setRuleName(request.getRuleName());
			distributionRules.setDeviceType(devicetype);
			distributionRules.setRuleDescription(request.getRuleDescription());
			distributionRules.setRuleJson(request.getRuleExpression());
			distributionRules.setLastUpdatedTime(new Date());
			distributionRules.setRuleUser(entityUser);
			distributionRules.setUalTemplate(true);
			distributionRules = distributionRuleRepository.save(distributionRules);

		}
		setMultipartFile.setDistributionRule(distributionRules);
		setMultipartFile.setDeviceType(devicetype);
		handle();
		// Make an entry in import_tracker of the current region as completed.
		int size = ualDetailsRepository.totalRecordsOfUalDetails(distributionRules.getId());
		ImportTracker findByRuleName = importTrackerRepository.findByRuleNameAndRegion(request.getRuleName(),
				"eu-west");
		if (findByRuleName == null) {
			ImportTracker impTracker = new ImportTracker();
			impTracker.setRegion(pocConfig.getRegion());
			impTracker.setStatus("COMPLETED");
			impTracker.setRuleName(request.getRuleName());
			ImportTracker save = importTrackerRepository.save(impTracker);
			if (save != null) {
				System.out.println("UAL Details saved successfully in " + save.getRegion());
			}
		} else {
			System.out.println("Ual Details already present and saved in " + pocConfig.getRegion());
		}
		request.setSize(size);
		notifyDss(request);
	}

	private void handle() throws Exception {
		JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis())
				.toJobParameters();
		JobExecution execution = jobLauncher.run(job, jobParameters);
		System.out.println("STATUS :: " + execution.getStatus());

	}

	private void notifyDss(RuleRequest ruleRequest) {
		try {
			rabbitTemplate.convertAndSend(pocConfig.getDistributionExchange(), pocConfig.getDistributionRoutingKey(), ruleRequest);
			log.info("Successfully published to RabbitMQ for rule name: " + ruleRequest.getRuleName());

		} catch (AmqpException exception) {
			log.error("unable to publish message into rabbitMQ for rule: " + ruleRequest.getRuleName() + ". Error: "
					+ exception);
		}

	}

	@Override
	public UalDetailsResponse getPagedData(RuleRequest ruleRequest, int pageNo) {
		DistributionRule findByRuleNameAndDeviceTypeDevicetypeName = distributionRuleRepository
				.findByRuleNameAndDeviceTypeDevicetypeName(ruleRequest.getRuleName(), ruleRequest.getProductType());
		Pageable pageable = PageRequest.of(pageNo, 100);
		Page<UalDetails> posts = ualDetailsRepository
				.findAllByDistributionRule(findByRuleNameAndDeviceTypeDevicetypeName, pageable);
		// get content for page object
		List<UalDetails> listOfUalDetails = posts.getContent();
		UalDetailsResponse postResponse = new UalDetailsResponse();
		postResponse.setContent(listOfUalDetails);
		postResponse.setPageNo(posts.getNumber());
		postResponse.setPageSize(posts.getSize());
		postResponse.setTotalElements(posts.getTotalElements());
		postResponse.setTotalPages(posts.getTotalPages());
		postResponse.setLast(posts.isLast());
		return postResponse;
	}

	@Override
	public void saveToImportTracker(@Valid StatusUpdateRequest updateRequest) {
		ImportTracker findByRuleName = importTrackerRepository.findByRuleNameAndRegion(updateRequest.getRule(),
				updateRequest.getRegion());
		if (findByRuleName == null) {
			ImportTracker imp = new ImportTracker();
			imp.setRegion(updateRequest.getRegion());
			imp.setRuleName(updateRequest.getRule());
			imp.setStatus(updateRequest.getStatus());
			ImportTracker save = importTrackerRepository.save(imp);
			if (save != null) {
				System.out.println("Inserted status of " + save.getRuleName() + " as " + save.getStatus() + " in "
						+ save.getRegion());
			}
		} else {
			findByRuleName.setStatus(updateRequest.getStatus());
			ImportTracker save = importTrackerRepository.save(findByRuleName);
			if (save != null) {
				System.out.println("Inserted status of " + save.getRuleName() + " as " + save.getStatus() + " in "
						+ save.getRegion());
			}
		}
	}

	@Override
	public void populateDB(RuleRequest ruleRequest, int totalRecords) throws FirmwareManagementException {
		System.out.println("Trying to create rule");
		// 1.Find the particular device type
		final DeviceType devicetype = deviceTypeRepository
				.findByDevicetypeNameOrDevicetypeDisplayname(ruleRequest.getProductType());
		DistributionRule distributionRules = null;
		// 2.Find whether a distribution rule exists or not
		distributionRules = distributionRuleRepository.findRuleByRuleNameAndPropositionName(ruleRequest.getRuleName(),
				ruleRequest.getBusiness());
		// 3.Create a rule if it already doesnt not exist
		if (distributionRules == null) {
			log.debug("Rule name " + ruleRequest.getRuleName() + "Not Exists under Business "
					+ ruleRequest.getBusiness());
			distributionRules = new DistributionRule();
			distributionRules.setRuleName(ruleRequest.getRuleName());
			distributionRules.setDeviceType(devicetype);
			distributionRules.setRuleDescription(ruleRequest.getRuleDescription());
			distributionRules.setRuleJson(ruleRequest.getRuleExpression());
			distributionRules.setLastUpdatedTime(new Date());
			distributionRules.setRuleUser("Tanksali, Pawan(Partner)");
			distributionRules.setUalTemplate(true);
			distributionRules = distributionRuleRepository.save(distributionRules);
			if (distributionRules != null) {
				System.out.println("Rule created with name" + distributionRules.getRuleName() + " successfully!");
			}
		}
		// 4.Find whether sync tracker entry is present or not for the particular rule
		SyncTracker findByRuleName = null;
		findByRuleName = syncTrackerRepository.findByRuleName(distributionRules.getRuleName());
		UalDetailsResponse response = null;
		// 5a.If entry isnt present then make a call to import paged with initial value,
		// i.e page 0
		if (findByRuleName == null) {
			response = callImportPaged(ruleRequest, 0);
		} else {
			// 5b.If an entry already present, then get the current page from DB and use the
			// current page as counter for processing other records.
			System.out.println("Sync tracker entry already exists for rule: " + findByRuleName.getRuleName()
					+ " and current page: " + findByRuleName.getCurrentPage());
			int count = findByRuleName.getCurrentPage();
			response = callImportPaged(ruleRequest, count);
			// 5b.a.Check to determine whether the maximum limit is reached
			if (response.getPageNo() == response.getTotalPages()) {
				throw new FirmwareManagementException(FmsErrorCodes.MAXIMUM_PAGES);
			}
		}
		// 6.If call to import paged fails, then throw exception.
		if (response == null) {
			throw new FirmwareManagementException(FmsErrorCodes.UNKNOWN_ERROR);
		}
		// 7.Set distribution rule for the ualdetails list, i.e paged data.
		for (UalDetails ualDetails : response.getContent()) {
			ualDetails.setDistributionRule(distributionRules);
		}
		// 8.Save the paged data in DB
		System.out.println("Trying to save UalDetails in DB");
		ualDetailsRepository.saveAll(response.getContent());
		System.out.println("Saved records in UalDetails table");
		// 9.Update current page number in sync tracker
		findByRuleName = syncTrackerRepository.findByRuleName(ruleRequest.getRuleName());
		// 10.If entry already present then update the current page as
		// response.getPage+1 else create new synctracker entry
		if (findByRuleName != null) {
			findByRuleName.setCurrentPage(response.getPageNo() + 1);
			findByRuleName.setTotalRecords(totalRecords);
			findByRuleName = syncTrackerRepository.save(findByRuleName);
			if (findByRuleName != null) {
				System.out.println("Successfully updated sync tracker for rule: " + findByRuleName.getRuleName()
						+ " with current page: " + findByRuleName.getCurrentPage());
			}
		} else {
			System.out.println("Entry in sync tracker doesnt exist, hence creating one");
			findByRuleName = new SyncTracker();
			findByRuleName.setRuleName(distributionRules.getRuleName());
			findByRuleName.setCurrentPage(1);
			findByRuleName.setTotalRecords(totalRecords);
			SyncTracker save = syncTrackerRepository.save(findByRuleName);
			if (save != null) {
				System.out.println("Successfully created entry in sync tracker with rule: " + save.getRuleName()
						+ " with current page: " + save.getCurrentPage());
			}
		}
		// 11.Update the counter to process other records.
		int count = findByRuleName.getCurrentPage();
		// 12.Looping thru the rest of the pages, call import paged for each page, save
		// it in DB and update sync tracker.
		for (int cnt = count; cnt < response.getTotalPages(); cnt++) {
			response = callImportPaged(ruleRequest, cnt);
			if (response == null) {
				throw new FirmwareManagementException(FmsErrorCodes.UNKNOWN_ERROR);
			}
			for (UalDetails ualDetails : response.getContent()) {
				ualDetails.setDistributionRule(distributionRules);
			}
			System.out.println("Trying to save in UalDetails table");
			ualDetailsRepository.saveAll(response.getContent());
			System.out.println("Saved in ualdetails");
			// 13.Update current page number in sync tracker
			findByRuleName = syncTrackerRepository.findByRuleName(ruleRequest.getRuleName());
			if (findByRuleName != null) {
				findByRuleName.setCurrentPage(response.getPageNo() + 1);
				findByRuleName = syncTrackerRepository.save(findByRuleName);
				if (findByRuleName != null) {
					System.out.println("Updated current page to " + findByRuleName.getCurrentPage() + " of rule: "
							+ findByRuleName.getRuleName());
				}
			}
		}
		// 14.Setting status whether its completed or not;
		String status = "";
		if (response.isLast()) {
			status = "COMPLETED";
		} else {
			status = "FAILED";
		}
		// 15.Call importualstatus api of the other region(master).
		StatusUpdateRequest updateRequest = new StatusUpdateRequest();
		updateRequest.setRegion(pocConfig.getRegion());
		updateRequest.setRule(distributionRules.getRuleName());
		updateRequest.setStatus(status);
		callImportStatus(updateRequest);
	}

	private UalDetailsResponse callImportPaged(RuleRequest ruleRequest, int count) throws FirmwareManagementException {
		ResponseEntity<UalDetailsResponse> response = null;
		try {
			String urlImport = pocConfig.getPocUrl() + "poc/$importPagedApi?pageNo=" + count;
			final Map<String, String> headermap = headerUtils.setHeaders();
			final HttpEntity<RuleRequest> requestEntity = headerUtils.createHttpEntityWithHeader(headermap, ruleRequest);
			System.out.println("Connecting to url: " + urlImport);
			System.out.println("Trying to fetch data from other region with page number: " + count);
			response = restTemplate.exchange(urlImport, HttpMethod.POST, requestEntity, UalDetailsResponse.class);
			System.out.println("Fetched " + response.getBody().getPageNo() + " page out of "
					+ response.getBody().getTotalPages() + " pages");
		} catch (HttpStatusCodeException ehttpStatusCodeException) {
			System.out.println(ehttpStatusCodeException.getResponseBodyAsString());
			throw new FirmwareManagementException(FmsErrorCodes.DELETE_PACKAGE_USEAST_DOWN);
		}
		return response.getBody();
	}

	private void callImportStatus(StatusUpdateRequest statusUpdate) {
		try {
			String urlImport = pocConfig.getPocUrl() + "poc/$importualstatus";
			final Map<String, String> headermap = headerUtils.setHeaders();
			final HttpEntity<StatusUpdateRequest> requestEntity = headerUtils.createHttpEntityWithHeader(headermap, statusUpdate);
			System.out.println("Connecting to url: " + urlImport);
			System.out.println("Finished saving in DB, now calling importualstatus of other region");
			System.out.println("Calling status api of rule: " + statusUpdate.getRule() + " of region: "
					+ statusUpdate.getRegion() + " with status: " + statusUpdate.getStatus());
			restTemplate.exchange(urlImport, HttpMethod.POST, requestEntity, Void.class);
			System.out.println("Triggered update status API of the other region");
		} catch (HttpStatusCodeException ehttpStatusCodeException) {
			System.out.println(ehttpStatusCodeException.getResponseBodyAsString());
		}
	}

}
