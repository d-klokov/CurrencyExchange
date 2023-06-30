package ru.klokov.datasource;

import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;

public class SQLiteDataSourceFactory implements DataSourceFactory {
    @Override
    public DataSource getDataSource() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite::resource:currency_exchange.db");

        return dataSource;
    }
}
