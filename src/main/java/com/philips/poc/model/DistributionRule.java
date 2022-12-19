package com.philips.poc.model;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DistributionRule {

	private static final long serialVersionUID = 1L;
    private int id;
    private int deviceTypeId;
    private String ruleName;
    private String ruleJson;
    private String ruleUser;
    private String ruleDescription;
    private Date lastUpdatedTime;
    private boolean ualTemplate;
    private String satelliteProductType;
    private List<String> equipmentNumbers;
}
