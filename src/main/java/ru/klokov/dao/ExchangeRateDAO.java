package ru.klokov.dao;

import ru.klokov.exception.DatabaseException;
import ru.klokov.model.Currency;
import ru.klokov.model.ExchangeRate;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDAO implements IExchangeRateDAO {
    private final DataSource dataSource;

    public ExchangeRateDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<ExchangeRate> findAll() throws DatabaseException {
        String sql = "SELECT * FROM exchange_rates";
        List<ExchangeRate> exchangeRates = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(sql)) {
                while (resultSet.next()) {
                    exchangeRates.add(
                            new ExchangeRate(
                                    resultSet.getLong("id"),
                                    resultSet.getLong("base_currency_id"),
                                    resultSet.getLong("target_currency_id"),
                                    resultSet.getDouble("rate")
                            ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error!");
        }
        return exchangeRates;
    }

//    @Override
//    public ExchangeRate findById(Long id){
//        String sql = "SELECT * FROM exchange_rates WHERE id = ?";
//        ExchangeRate exchangeRate = null;
//
//        try (Connection connection = dataSource.getConnection();
//             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
//
//            preparedStatement.setLong(1, id);
//            try (ResultSet resultSet = preparedStatement.executeQuery()) {
//                if (resultSet.next()) {
//                    exchangeRate = new ExchangeRate(
//                            resultSet.getLong("id"),
//                            resultSet.getLong("base_currency_id"),
//                            resultSet.getLong("target_currency_id"),
//                            resultSet.getDouble("rate")
//                    );
//                }
//            }
//        } catch (SQLException e) {
//            throw new DatabaseException("Database error!");
//        }
//
//        if (exchangeRate == null) {
//            throw new ResourceNotFoundException("Exchange rate with " + id + " not found!");
//        }
//
//        return exchangeRate;
//    }

    @Override
    public Optional<ExchangeRate> findByCurrencyPair(Currency baseCurrency, Currency targetCurrency) throws DatabaseException {
        String sql = "SELECT * FROM exchange_rates WHERE base_currency_id = ? AND target_currency_id = ?";
        ExchangeRate exchangeRate = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, baseCurrency.getId());
            preparedStatement.setLong(2, targetCurrency.getId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    exchangeRate = new ExchangeRate(
                            resultSet.getLong("id"),
                            resultSet.getLong("base_currency_id"),
                            resultSet.getLong("target_currency_id"),
                            resultSet.getDouble("rate")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error!");
        }

        return Optional.ofNullable(exchangeRate);
    }

    @Override
    public ExchangeRate save(ExchangeRate exchangeRate) throws DatabaseException {
        String sql = "INSERT INTO exchange_rates (base_currency_id, target_currency_id, rate) VALUES (?, ?, ?)";
        int rows;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, exchangeRate.getBaseCurrencyId());
            preparedStatement.setLong(2, exchangeRate.getTargetCurrencyId());
            preparedStatement.setDouble(3, exchangeRate.getRate());

            rows = preparedStatement.executeUpdate();

            if (rows != 0) {
                try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                    if (keys.next()) {
                        exchangeRate.setId(keys.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error!");
        }

        return exchangeRate;
    }

    @Override
    public ExchangeRate update(ExchangeRate exchangeRate, double newRate) throws DatabaseException {
        String sql = "UPDATE exchange_rates SET base_currency_id = ?, target_currency_id = ?, rate = ? WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, exchangeRate.getBaseCurrencyId());
            preparedStatement.setLong(2, exchangeRate.getTargetCurrencyId());
            preparedStatement.setDouble(3, newRate);
            preparedStatement.setLong(4, exchangeRate.getId());

            preparedStatement.executeUpdate();

            exchangeRate.setRate(newRate);
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }

        return exchangeRate;
    }
}
