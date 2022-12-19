package com.philips.poc.model;


import java.util.Date;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class UalDetailsDto {

	private long id;
	

	private String equipmentNumber;
	
	
	private String status;
	
	
	private String createdBy;
	
	
	private Date ualCreationTime;
}
