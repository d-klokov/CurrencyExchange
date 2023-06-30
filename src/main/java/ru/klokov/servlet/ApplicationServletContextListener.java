package ru.klokov.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import ru.klokov.dao.CurrencyDAO;
import ru.klokov.dao.ExchangeRateDAO;
import ru.klokov.datasource.SQLiteDataSourceFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

@WebListener
public class ApplicationServletContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        DataSource sqliteDataSource = new SQLiteDataSourceFactory().getDataSource();
        CurrencyDAO currencyDAO = new CurrencyDAO(sqliteDataSource);

        initDataBase(sqliteDataSource);

        context.setAttribute("mapper", new ObjectMapper());
        context.setAttribute("currencyDAO", currencyDAO);
        context.setAttribute("exchangeRateDAO", new ExchangeRateDAO(sqliteDataSource));
    }

    private void initDataBase(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement())
        {
            String sql = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(
                    getClass().getClassLoader().getResource("init.sql")).toURI())));
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("DataBase error!");
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("SQL query file error!");
        }
    }
}
