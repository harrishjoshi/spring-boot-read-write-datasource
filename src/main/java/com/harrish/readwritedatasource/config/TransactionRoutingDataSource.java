package com.harrish.readwritedatasource.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;

@Slf4j
public class TransactionRoutingDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<DataSourceType> currentDataSource = new ThreadLocal<>();

    public TransactionRoutingDataSource(DataSource master, DataSource read) {
        var dataSources = new HashMap<>();
        dataSources.put(DataSourceType.READ_WRITE, master);
        dataSources.put(DataSourceType.READ_ONLY, read);

        super.setTargetDataSources(dataSources);
        super.setDefaultTargetDataSource(master);
    }

    static void setReadonlyDataSource(boolean isReadonly) {
        currentDataSource.set(isReadonly ? DataSourceType.READ_ONLY : DataSourceType.READ_WRITE);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        var dataSourceType = currentDataSource.get();
        log.info("Current dataSourceType: {}", dataSourceType);

        return dataSourceType;
    }

    enum DataSourceType {
        READ_ONLY,
        READ_WRITE
    }
}

