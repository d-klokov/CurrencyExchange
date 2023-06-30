package ru.klokov.dao;

import ru.klokov.exception.DatabaseException;
import ru.klokov.model.Currency;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyDAO implements ICurrencyDAO {
    private final DataSource dataSource;

    public CurrencyDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Currency> findAll() throws DatabaseException {
        String sql = "SELECT * FROM currencies";
        List<Currency> currencies;

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(sql)) {
                currencies = new ArrayList<>();
                while (resultSet.next()) {
                    currencies.add(
                            new Currency(
                                    resultSet.getLong("id"),
                                    resultSet.getString("code"),
                                    resultSet.getString("full_name"),
                                    resultSet.getString("sign")
                            )
                    );
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error!");
        }

        return currencies;
    }

    @Override
    public Optional<Currency> findById(Long id) throws DatabaseException {
        String sql = "SELECT * FROM currencies WHERE id = ?";
        Currency currency = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    currency = new Currency(
                            resultSet.getLong("id"),
                            resultSet.getString("code"),
                            resultSet.getString("full_name"),
                            resultSet.getString("sign")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error!");
        }

        return Optional.ofNullable(currency);
    }

    @Override
    public Optional<Currency> findByCode(String code) throws DatabaseException {
        String sql = "SELECT * FROM currencies WHERE code = ?";
        Currency currency = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, code);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    currency = new Currency(
                            resultSet.getLong("id"),
                            resultSet.getString("code"),
                            resultSet.getString("full_name"),
                            resultSet.getString("sign")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error!");
        }

        return Optional.ofNullable(currency);
    }

    @Override
    public Currency save(Currency currency) throws DatabaseException {
        String sql = "INSERT INTO currencies (code, full_name, sign) VALUES (?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, currency.getCode());
            preparedStatement.setString(2, currency.getFullName());
            preparedStatement.setString(3, currency.getSign());

            int rows = preparedStatement.executeUpdate();

            if (rows > 0) {
                try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                    if (keys.next()) {
                        currency.setId(keys.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error!");
        }

        return currency;
    }
}
