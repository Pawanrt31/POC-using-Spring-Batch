package com.philips.poc.step;

import com.philips.poc.model.UalEquipmentNumber;
import com.philips.poc.service.impl.SetMultipartFile;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@StepScope
public class Reader implements ItemReader<UalEquipmentNumber> {

	private static int counter = 0;
	
	@Autowired
	private SetMultipartFile setMultipartFile;
	
	@Override
	public UalEquipmentNumber read()
			throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		List<String> equipmentNumbers = setMultipartFile.getEquipmentNumbers();
		if(counter == equipmentNumbers.size()) {
			counter = 0;
			return null;
		}
		UalEquipmentNumber ualEquipmentNumber = new UalEquipmentNumber();
		ualEquipmentNumber.setEquipmentNumber(equipmentNumbers.get(counter));
		counter++;
		return ualEquipmentNumber;
	}
}
	
