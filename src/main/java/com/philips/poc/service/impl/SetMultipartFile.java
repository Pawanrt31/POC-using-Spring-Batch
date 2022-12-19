package com.philips.poc.service.impl;

import com.philips.poc.entity.DeviceType;
import com.philips.poc.entity.DistributionRule;
import com.philips.poc.model.RuleRequest;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class SetMultipartFile {

	private MultipartFile multipartFile;

	private RuleRequest ruleRequest;

	private String entityUser;

	private DistributionRule distributionRule;

	private DeviceType deviceType;
	
	private List<String> equipmentNumbers;

	public void setFile(MultipartFile multipartFile) {
		this.multipartFile = multipartFile;
	}

	public InputStream getInputStream() throws IOException {
		return this.multipartFile.getInputStream();
	}

	public RuleRequest getRuleRequest() {
		return this.ruleRequest;
	}

	public void setRuleRequest(RuleRequest ruleRequest) {
		this.ruleRequest = ruleRequest;
	}

	public String getEntityUser() {
		return this.entityUser;
	}

	public void setEntityUser(String entityUser) {
		this.entityUser = entityUser;
	}

	public void setDistributionRule(DistributionRule distRule) {
		this.distributionRule = distRule;
	}

	public DistributionRule getDistributionRule() {
		return this.distributionRule;
	}

	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}

	public DeviceType getDeviceType() {
		return this.deviceType;
	}
	
	public void setEquipmentNumbers(List<String> equipmentNumbers) {
		this.equipmentNumbers = equipmentNumbers;
	}
	
	public List<String> getEquipmentNumbers() {
		return this.equipmentNumbers;
	}
}
