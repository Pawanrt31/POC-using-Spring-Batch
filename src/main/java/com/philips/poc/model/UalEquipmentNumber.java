package com.philips.poc.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UalEquipmentNumber {

	private String equipmentNumber;
	
	private RuleRequest ruleRequest;

	private String entityUser;
	
	
}
