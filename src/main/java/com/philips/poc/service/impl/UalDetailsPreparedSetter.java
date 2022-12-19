package com.philips.poc.service.impl;

import com.philips.poc.entity.UalDetails;

import org.springframework.batch.item.database.ItemPreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UalDetailsPreparedSetter implements ItemPreparedStatementSetter<UalDetails> {

	
	@Override
	public void setValues(UalDetails item, PreparedStatement ps) throws SQLException {
		// TODO Auto-generated method stub
		ps.setInt(1, item.getDistributionRule().getId());
		ps.setString(2, item.getEquipmentNumber());
		ps.setString(3, item.getStatus());
		ps.setString(4, item.getCreatedBy());
		ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
		
	}
}
