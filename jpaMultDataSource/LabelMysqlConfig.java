package com.mycompany.server.config.mySQLDataSourceConfig;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@PropertySource({"classpath:application.properties"})
@EnableJpaRepositories(basePackages = "com.mycompany.server.repo.mysql.label_test",
        entityManagerFactoryRef = "userEntityManager",
        transactionManagerRef = "userTransactionManager"
)
public class LabelMysqlConfig extends BaseJpaConfig{
    @Autowired
    private Environment env;

    @Bean(name = "userEntityManager")
    public LocalContainerEntityManagerFactoryBean userEntityManager() {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(userDataSource());
        factory.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        factory.setPackagesToScan("com.mycompany.server.entity.mysql.label_test");
        factory.setJpaProperties(hibernateProperties());
        return factory;
    }

    @Bean
    @ConfigurationProperties(prefix = "datasource.primary")
    public DataSource userDataSource() {
        DriverManagerDataSource dataSource
                = new DriverManagerDataSource();
        dataSource.setDriverClassName(
                env.getProperty("jdbc.driverClassName"));
        dataSource.setUrl(env.getProperty("spring.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.password"));

        return dataSource;
    }

    @Bean(name = "mysqlJdbcTemplate")
    public JdbcTemplate mysqlJdbcTemplate() {
        return new JdbcTemplate(userDataSource());
    }

    @Bean(name = "userTransactionManager")
    @Primary
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(userEntityManager().getObject());
        return transactionManager;
    }
}