package com.philips.poc.service.impl;

import com.philips.poc.model.UalEquipmentNumber;

import org.springframework.batch.item.excel.RowMapper;
import org.springframework.batch.item.excel.support.rowset.RowSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RowMapperImpl implements RowMapper<UalEquipmentNumber> {

	@Autowired
	private SetMultipartFile setMultipartFile;

	public RowMapperImpl() {
	}

	@Override
	public UalEquipmentNumber mapRow(RowSet rs) throws Exception {
		if (rs == null || rs.getCurrentRow() == null) {
			return null;
		}
		System.out.println("Entered maprow " + setMultipartFile);
		UalEquipmentNumber bl = new UalEquipmentNumber();
		bl.setEquipmentNumber(rs.getColumnValue(24));
		bl.setEntityUser(setMultipartFile.getEntityUser());
		bl.setRuleRequest(setMultipartFile.getRuleRequest());
		System.out.println("End of maprow");
		return bl;
	}

}
