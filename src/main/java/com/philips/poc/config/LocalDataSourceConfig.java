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

import com.zaxxer.hikari.HikariDataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("default")
/**
 * The Class LocalDataSourceConfig.
 */
public class LocalDataSourceConfig {

    /** The driver class name. */
    @Value("${database.driverClassName}")
    public String driverClassName;

    /** The username. */
    @Value("${database.username}")
    private String username;

    /** The password. */
    @Value("${database.password}")
    private String password;

    /** The database url. */
    @Value("${database.url}")
    private String databaseUrl;

    @Value("${database.connectionTimeout}")
    private long connectionTimeout;

    @Value("${database.minimumIdle}")
    private Integer minimumIdleTime;

    @Value("${database.maximumPoolSize}")
    private Integer maximumPoolSize;

    @Value("${database.idleTimeout}")
    private long idleTimeout;

    @Value("${database.maxLifeTimeout}")
    private long maxLifeTime;

    /**
     * Local source properties.
     *
     * @return the data source properties
     */
    @Primary
    @Bean
    /**
     * creating DataSourceProperties for local db
     * 
     * @return
     */
    public DataSource localSourceProperties() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setMaximumPoolSize(maximumPoolSize);
        hikariDataSource.setConnectionTimeout(connectionTimeout);
        hikariDataSource.setMinimumIdle(minimumIdleTime);
        hikariDataSource.setMaxLifetime(maxLifeTime);
        hikariDataSource.setIdleTimeout(idleTimeout);
        DataSourceProperties prob = new DataSourceProperties();
        prob.setUrl(databaseUrl);
        prob.setUsername(username);
        prob.setPassword(password);
        prob.setDriverClassName(driverClassName);
        hikariDataSource.setDataSource(prob.initializeDataSourceBuilder().build());
        return hikariDataSource;
    }
}
