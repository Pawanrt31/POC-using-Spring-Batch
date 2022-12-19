package com.philips.poc.step;

import com.philips.poc.entity.DistributionRule;
import com.philips.poc.entity.UalDetails;
import com.philips.poc.model.UalEquipmentNumber;
import com.philips.poc.service.impl.SetMultipartFile;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@StepScope
public class Processor implements ItemProcessor<UalEquipmentNumber, UalDetails> {

	@Autowired
	private SetMultipartFile setMultipartFile;


	@Override
	public UalDetails process(UalEquipmentNumber item) throws Exception {
		String entityUser = setMultipartFile.getEntityUser();
		DistributionRule distributionRules = setMultipartFile.getDistributionRule();
		UalDetails ualDetail = new UalDetails();
		ualDetail.setDistributionRule(distributionRules);
		ualDetail.setEquipmentNumber(item.getEquipmentNumber());
		ualDetail.setStatus(null);
		ualDetail.setCreatedBy(entityUser);
		ualDetail.setPackageCreationTime(new Date());
		return ualDetail;

	}

}
