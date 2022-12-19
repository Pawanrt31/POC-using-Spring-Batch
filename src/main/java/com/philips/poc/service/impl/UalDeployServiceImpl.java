package com.philips.poc.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.philips.poc.config.PocConfig;
import com.philips.poc.entity.DistributionProcessor;
import com.philips.poc.entity.DistributionRule;
import com.philips.poc.entity.UalDetails;
import com.philips.poc.model.DistributionProcessRequest;
import com.philips.poc.model.UalDeployRequest;
import com.philips.poc.repository.DistributionProcessorRepository;
import com.philips.poc.repository.DistributionRuleRepository;
import com.philips.poc.repository.PagingRepository;
import com.philips.poc.service.UalDeployService;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class UalDeployServiceImpl implements UalDeployService{

	@Autowired
	private PagingRepository pagingRepository;
	@Autowired
	private DistributionRuleRepository distributionRuleRepository;
	@Autowired
	private DistributionProcessorRepository distributionProcessorRepository;
	@Autowired
	private RabbitTemplate rabbitTemplate;
	@Autowired
	private PocConfig pocConfig;
	@Autowired
    private ObjectMapper objectMapper;
	
	private static final String SEPARATOR = ",";
	
	public static final Integer INTEGER_ZERO = Integer.valueOf(0);

//	@Override
//	public void createEntryInDBAndDropMessage(String packageId, String ruleName, String distributionRefId)
//			throws JsonProcessingException {
	
	@Override
	public void createEntryInDBAndDropMessage(UalDeployRequest ualDeployRequest)
			throws JsonProcessingException {
		System.out.println("Fetching rule with rule_name: "+ualDeployRequest.getRuleName());
		DistributionRule distributionRule = distributionRuleRepository.findByRuleNameAndDeviceTypeDevicetypeName(ualDeployRequest.getRuleName(),
				ualDeployRequest.getProductType());
		Pageable pageable = PageRequest.of(0, 100);
		System.out.println("Fetching paged data of 100 records from ual details by rulename");
		Page<UalDetails> softwarePackageList = pagingRepository.findAllByDistributionRule(distributionRule, pageable);
		String selectedDevice = null;
		if (null != softwarePackageList && !softwarePackageList.getContent().isEmpty()) {
			selectedDevice = String.join(SEPARATOR, softwarePackageList.getContent().stream()
					.map(soft -> soft.getEquipmentNumber()).collect(Collectors.toList()));
		}
		System.out.println("Created selected devices");
		DistributionProcessor distributionProcessorRequest = new DistributionProcessor();
		distributionProcessorRequest.setDistributionDetails(ualDeployRequest.getDistributionRefId());
		distributionProcessorRequest.setSoftwarePackage(ualDeployRequest.getPackageId());
		distributionProcessorRequest.setTotalData(100);
		distributionProcessorRequest.setBatchSize(100);
		distributionProcessorRequest.setCurrentPage(0);
		distributionProcessorRequest.setDistributionType("UalAssociate");
		distributionProcessorRequest.setProcessType("UalImport");
		distributionProcessorRequest.setSelectedDevices(selectedDevice);
		distributionProcessorRequest.setStatus("null");
		System.out.println("Trying to save in distribution processor table");
		distributionProcessorRepository.save(distributionProcessorRequest);
		System.out.println("Saved in distribution processor table");
		createDistributionProcessorData(1, distributionRule, ualDeployRequest.getDistributionRefId(), ualDeployRequest.getPackageId(), softwarePackageList.getTotalPages());
		System.out.println("Trying to publish to RabbitMQ");
		createMsgIntoRabbitMq(ualDeployRequest.getDistributionRefId());
		
	}
	
	private void createDistributionProcessorData(int pageNumber, DistributionRule distributionRule,
			String distributionRefId, String packageId, int total) {
		
		Pageable pageable = PageRequest.of(pageNumber, 100);
		System.out.println("Fetching paged data of 100 records from ual details by rulename");
		Page<UalDetails> softwarePackageList = pagingRepository.findAllByDistributionRule(distributionRule, pageable);
		String selectedDevice = null;
		if (null != softwarePackageList && !softwarePackageList.isEmpty()) {
			selectedDevice = String.join(SEPARATOR, softwarePackageList.getContent().stream()
					.map(soft -> soft.getEquipmentNumber()).collect(Collectors.toList()));
		}
		
		DistributionProcessor distributionProcessorRequest = new DistributionProcessor();
		distributionProcessorRequest.setDistributionDetails(distributionRefId);
		distributionProcessorRequest.setSoftwarePackage(packageId);
		distributionProcessorRequest.setTotalData(100);
		distributionProcessorRequest.setBatchSize(100);
		distributionProcessorRequest.setCurrentPage(0);
		distributionProcessorRequest.setDistributionType("UalAssociate");
		distributionProcessorRequest.setProcessType("UalImport");
		distributionProcessorRequest.setSelectedDevices(selectedDevice);
		distributionProcessorRequest.setStatus("null");
		System.out.println("Trying to save in distribution processor table");
		distributionProcessorRepository.save(distributionProcessorRequest);
		System.out.println("Saved in distribution processor table");
		if (++pageNumber < total) {
			createDistributionProcessorData(pageNumber, distributionRule, distributionRefId, packageId,
					softwarePackageList.getTotalPages());
		}
		
	}

	/**
	 * publish the message into rabbitMq
	 * @throws JsonProcessingException 
	 */
	public void createMsgIntoRabbitMq(String distributionRefId) throws JsonProcessingException {
		DistributionProcessRequest distributionProcessRequest = new DistributionProcessRequest();
		distributionProcessRequest.setDistributionRefId(distributionRefId);
		distributionProcessRequest.setDistributionType("UalAssociate");
		distributionProcessRequest.setResourceType("ASSOCIATE");
		distributionProcessRequest.setType("BATCH");
		createAndSendMessage(distributionProcessRequest);
	}
	
	public void createAndSendMessage(Object payload) throws JsonProcessingException {
        Message message;
        try {
            byte[] data = objectMapper.writeValueAsBytes(payload);

            message = MessageBuilder.withBody(data).setContentType(MediaType.APPLICATION_JSON_VALUE)
                                            .setHeader("RETRY_COUNT", NumberUtils.INTEGER_ZERO)
                                            .build();
            //log.info("Dropping message to queue {}...", applicationProperties.getFms().getDistProcessor().getQueue());
            rabbitTemplate.convertAndSend(pocConfig.getDeployExchange(),
					pocConfig.getDeployRoutingKey(), message);
			System.out.println("Dropping message completed.");
            //log.info("Dropping message completed.");
        } catch (AmqpException exception) {
			System.out.println("error");
		}
    }
}
