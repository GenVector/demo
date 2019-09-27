package com.mycompany.server.config.mySQLDataSourceConfig;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@PropertySource({"classpath:application.properties"})
@EnableJpaRepositories(basePackages = "com.mycompany.server.repo.mysql.miami",
        entityManagerFactoryRef = "productEntityManager",
        transactionManagerRef = "productTransactionManager"
)
public class TravelMysqlConfig extends BaseJpaConfig {
    @Autowired
    private Environment env;

    @Bean(name = "productEntityManager")
    public LocalContainerEntityManagerFactoryBean productEntityManager() {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(productDataSource());
        factory.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        factory.setPackagesToScan("com.mycompany.server.entity.mysql.miami");
        factory.setJpaProperties(hibernateProperties());
        return factory;
    }

    @Bean
    @ConfigurationProperties(prefix = "datasource.secondary")
    public DataSource productDataSource() {
        DriverManagerDataSource dataSource
                = new DriverManagerDataSource();
        dataSource.setDriverClassName(
                env.getProperty("jdbc.driverClassName"));
        dataSource.setUrl(env.getProperty("spring.secondDatasource.url"));
        dataSource.setUsername(env.getProperty("spring.secondDatasource.username"));
        dataSource.setPassword(env.getProperty("spring.secondDatasource.password"));

        return dataSource;
    }

    @Bean(name = "productTransactionManager")
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(productEntityManager().getObject());
        return transactionManager;
    }
}