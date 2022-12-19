/**
* (C) Koninklijke Philips Electronics N.V. 2020
*
* All rights are reserved. Reproduction or transmission in whole or in part,
* in  any form or by any means, electronic, mechanical or otherwise, is
* prohibited without the prior written permission of the copyright owner.
* 
* @author Gaurav Mamgain
* 
* 
*/
package com.philips.poc.config;

import com.philips.poc.error.FirmwareManagementException;
import com.philips.poc.error.FmsErrorCodes;
import com.philips.poc.constant.Constants;
import com.zaxxer.hikari.HikariDataSource;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.common.RelationalServiceInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.util.CollectionUtils;

import java.util.List;

import javax.sql.DataSource;

/**
 * The Class CloudRegionsConfig.
 */
@Configuration
@Profile("cloud")
public class CloudDataSourceConfig {

    @Value("${DBNAME}")
    public String databaseName;

    @Value("${DATABASEDRIVER}")
    public String driverClassName;

    @Value("${DBSCHEMA}")
    private String dbSchema;

    @Autowired
    private Cloud cloud;

    public static final String CURRENT_SCHEMA = "?currentSchema=";
    public static final String JDBC_URL_START = "jdbc:postgresql://";

    @Bean
    /**
     * Cloud factory instance need to be instantiated
     * 
     * @return
     */
    public Cloud cloud() {
    	System.out.println("inside cloud()");
        return new CloudFactory().getCloud();
    }
    
    @Bean
    /**
     * Create rabbitMQ ConnectionFactory
     * 
     * @return ConnectionFactory
     */
    @Primary
	public ConnectionFactory rabbitConnectionFactory() {
		Cloud cloud = new CloudFactory().getCloud();
		return cloud.getSingletonServiceConnector(ConnectionFactory.class, null);
	}

    @Bean
    @Primary
    /**
     * creating data source properties from CF
     * 
     * @return
     */
    public DataSourceProperties cloudDataSourceProperties() throws FirmwareManagementException {
    	System.out.println("Inside clouddatasourceproperties");
        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        List<ServiceInfo> list = cloud.getServiceInfos(DataSource.class);
        if (!CollectionUtils.isEmpty(list)) {
            RelationalServiceInfo info = (RelationalServiceInfo) list.get(0);
            dataSourceProperties.setUrl(generateJDBCUrlFromServiceInfo(info));
            dataSourceProperties.setUsername(info.getUserName());
            dataSourceProperties.setPassword(info.getPassword());
            dataSourceProperties.setDriverClassName(driverClassName);

        } else {
            throw new FirmwareManagementException(FmsErrorCodes.CLOUD_DATASOURCE_CREATION_FAILED);
        }
        return dataSourceProperties;

    }

    @Bean
    @Primary
    @ConfigurationProperties(Constants.DATA_SOURCE_HIKARI)
    /**
     * creating connectionPool using hikari
     * 
     * @param cloudDataSourceProperties
     * @return
     */
    public DataSource dataSource(DataSourceProperties cloudDataSourceProperties) {
    	
    	System.out.println("Inside datasource()");
        return cloudDataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    /**
     * creating jdbc url along with application name
     * 
     * @param info
     * @return
     */
    private String generateJDBCUrlFromServiceInfo(RelationalServiceInfo info) {
    	System.out.println("Inside generate");
        StringBuilder jdbcUrl = new StringBuilder(JDBC_URL_START);
        jdbcUrl.append(info.getHost()).append(Constants.STR_COLON).append(info.getPort())
                                        .append(Constants.FORWARDSLASH).append(databaseName).append(CURRENT_SCHEMA)
                                        .append(dbSchema);
        return jdbcUrl.toString();
    }

}
