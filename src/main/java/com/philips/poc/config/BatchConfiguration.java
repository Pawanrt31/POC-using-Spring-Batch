package com.philips.poc.config;

import com.philips.poc.entity.UalDetails;
import com.philips.poc.listener.JobCompletionListener;
import com.philips.poc.model.UalEquipmentNumber;
import com.philips.poc.service.impl.UalDetailsPreparedSetter;
import com.philips.poc.step.Processor;
import com.philips.poc.step.Reader;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private Processor processor;

	@Autowired
	private Reader reader;

	@Bean
	public Job processJob() throws Exception {
		return jobBuilderFactory.get("processJob").incrementer(new RunIdIncrementer()).listener(listener())
				.flow(orderStep1()).end().build();
	}

	@Bean
	public Step orderStep1() throws Exception {
		return stepBuilderFactory.get("orderStep1").<UalEquipmentNumber, UalDetails>chunk(5000).reader(reader)
				.processor(processor).writer(writer(null, null)).build();
	}

	private static final String QUERY_INSERT_STUDENT = "INSERT "
			+ "INTO tbl_ual_details(rule_id, equipment_number, status, created_by, created_time) "
			+ "VALUES (?, ?, ?, ?, ?)";

	@Bean
	ItemWriter<UalDetails> writer(DataSource dataSource, NamedParameterJdbcTemplate jdbcTemplate) {
		JdbcBatchItemWriter<UalDetails> databaseItemWriter = new JdbcBatchItemWriter<>();
		databaseItemWriter.setDataSource(dataSource);
		databaseItemWriter.setJdbcTemplate(jdbcTemplate);

		databaseItemWriter.setSql(QUERY_INSERT_STUDENT);

		ItemPreparedStatementSetter<UalDetails> paramProvider = new UalDetailsPreparedSetter();
		databaseItemWriter.setItemPreparedStatementSetter(paramProvider);
		return databaseItemWriter;
	}

	@Bean
	public JobExecutionListener listener() {
		return new JobCompletionListener();
	}
}