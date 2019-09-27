package com.mycompany.server.config.mySQLDataSourceConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateExceptionTranslator;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
public class BaseJpaConfig {

    private static final String HIBERNATE_DIALECT = "hibernate.dialect";
    private static final String HIBERNATE_SHOW_SQL = "hibernate.show.sql";

    @Resource
    protected Environment env;

    @Bean
    public HibernateExceptionTranslator hibernateExceptionTranslator() {
        return new HibernateExceptionTranslator();
    }

    protected Properties hibernateProperties() {
        Properties properties = new Properties();
//        properties.put(HIBERNATE_DIALECT, env.getRequiredProperty(HIBERNATE_DIALECT));
//        properties.put(HIBERNATE_SHOW_SQL, env.getRequiredProperty(HIBERNATE_SHOW_SQL));
        properties.put("hibernate.physical_naming_strategy",
                "org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy");
        return properties;
    }
}