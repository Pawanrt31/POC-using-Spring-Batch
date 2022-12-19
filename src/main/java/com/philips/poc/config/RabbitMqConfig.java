/**
 * (C) Koninklijke Philips Electronics N.V. 2022
 * 
 * All rights are reserved. Reproduction or transmission in whole or in part, 
 * in  any form or by any means, electronic, mechanical or otherwise, is 
 * prohibited without the prior written permission of the copyright owner.
 * 
 * @author spanda
 */
package com.philips.poc.config;

import com.philips.poc.model.RuleRequest;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * the class contains all rabbitMQ configuration
 */
@Configuration
public class RabbitMqConfig {

	@Autowired
	private PocConfig pocConfig;

	/**
	 * Create a Scheduler Queue for all the scheduler
	 * 
	 * @return Queue
	 */
	@Bean
	public Queue schedulerQueue() {
		return new Queue(pocConfig.getDistributionQueue(), true);
	}

	/**
	 * Direct exchange created for all scheduler
	 * 
	 * @return DirectExchange
	 */
	@Bean
	public DirectExchange schedulerExchange() {
		return new DirectExchange(pocConfig.getDistributionExchange());
	}

	/**
	 * Binding schedulerExchange exchange and SchedulerQueue Queue with Routing key
	 * 
	 * @param queue
	 * @param exchange
	 * @return Binding
	 */
	@Bean
	public Binding bindingQueueWithExchange(@Qualifier("schedulerQueue") Queue queue,
			@Qualifier("schedulerExchange") DirectExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with(pocConfig.getDistributionRoutingKey());
	}

	/*
	 * Define rabbitMq message format mapping
	 */
	@Bean
	public DefaultClassMapper classMapper() {
		final DefaultClassMapper classMapper = new DefaultClassMapper();
		classMapper.setDefaultType(RuleRequest.class);
		return classMapper;
	}

	/**
	 * Direct exchange created for all scheduler
	 * 
	 * @return DirectExchange
	 */
	@Bean
	public DirectExchange deployExchange() {
		return new DirectExchange(pocConfig.getDeployExchange());
	}
	
	/**
	 * Create a Scheduler Queue for all the scheduler
	 * 
	 * @return Queue
	 */
	@Bean
	public Queue deployQueue() {
		return new Queue(pocConfig.getDeployQueue(), true);
	}

	

	/**
	 * Binding schedulerExchange exchange and SchedulerQueue Queue with Routing key
	 * 
	 * @param queue
	 * @param exchange
	 * @return Binding
	 */
	@Bean
	public Binding bindingDeployQueueWithExchange(@Qualifier("deployQueue") Queue queue,
			@Qualifier("deployExchange") DirectExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with(pocConfig.getDeployRoutingKey());
	}
}
