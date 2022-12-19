package com.philips.poc.ualprocessor;
//package com.philips.poc.UalProcessor;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.List;
//
//import org.springframework.batch.core.BatchStatus;
//import org.springframework.batch.core.JobExecution;
//import org.springframework.batch.core.listener.JobExecutionListenerSupport;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.core.RowMapper;
//import org.springframework.stereotype.Component;
//
//import com.philips.poc.entity.UalDetails;
//import com.philips.poc.model.UalEquipmentNumber;
//
//@Component
//public class JobCompletionNotificationListener extends JobExecutionListenerSupport
//{
//	private final JdbcTemplate jdbcTemplate;
//
//	@Autowired
//	public JobCompletionNotificationListener(JdbcTemplate jdbcTemplate)
//	{
//		this.jdbcTemplate = jdbcTemplate;
//	}
//
//	@Override
//	public void afterJob(JobExecution jobExecution)
//	{
//
//		if (jobExecution.getStatus() == BatchStatus.COMPLETED)
//		{
//			System.out.println(" -------- JOB FINISHED ------------------ ");
//
//			List<UalDetails> results = jdbcTemplate.query("SELECT name,age,salary FROM employee",
//					new RowMapper<UalDetails>()
//					{
//
//						@Override
//						public UalDetails mapRow(ResultSet rs, int row) throws SQLException
//						{
//							return new UalDetails(rs.getString(1), rs.getInt(2),rs.getInt(3);
//						}
//					});
//
//			for (UalDetails ualDetails : results)
//			{
//				System.out.println("Found <" + ualDetails + "> in the database.");
//			}
//		}
//
//	}
//}
