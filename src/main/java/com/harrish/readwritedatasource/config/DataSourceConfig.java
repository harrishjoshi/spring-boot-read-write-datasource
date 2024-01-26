package com.harrish.readwritedatasource.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel.TRACE;

@Configuration
@EntityScan
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "app.datasource.master")
    public DataSource readWriteConfiguration() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @ConfigurationProperties(prefix = "app.datasource.read")
    public DataSource readOnlyConfiguration() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @Primary
    public DataSource routingDataSource() {
        return new TransactionRoutingDataSource(
                loggingProxy("read_write", readWriteConfiguration()),
                loggingProxy("read_only", readOnlyConfiguration())
        );
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        var entityManager = new LocalContainerEntityManagerFactoryBean();
        entityManager.setDataSource(routingDataSource());
        entityManager.setPackagesToScan("com.harrish.readwritedatasource.model");

        var vendorAdapter = new HibernateJpaVendorAdapter();
        entityManager.setJpaVendorAdapter(vendorAdapter);
        entityManager.setJpaPropertyMap(jpaProperties());

        return entityManager;
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(@Qualifier("jpaTxManager") PlatformTransactionManager wrapped) {
        return new ReplicaAwareTransactionManager(wrapped);
    }

    @Bean(name = "jpaTxManager")
    public PlatformTransactionManager jpaTransactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    private DataSource loggingProxy(String name, DataSource dataSource) {
        var loggingListener = new SLF4JQueryLoggingListener();
        loggingListener.setLogLevel(TRACE);
        loggingListener.setLogger(name);
        loggingListener.setWriteConnectionId(false);

        return ProxyDataSourceBuilder.create(dataSource)
                .name(name)
                .listener(loggingListener)
                .build();
    }

    private Map<String, Object> jpaProperties() {
        var props = new HashMap<String, Object>();
        props.put("hibernate.physical_naming_strategy", CamelCaseToUnderscoresNamingStrategy.class.getName());
        props.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());

        return props;
    }
}
