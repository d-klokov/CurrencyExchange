package ru.klokov.datasource;

import javax.sql.DataSource;

public interface DataSourceFactory {
    DataSource getDataSource();
}
