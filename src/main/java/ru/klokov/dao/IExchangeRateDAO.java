package ru.klokov.dao;

import ru.klokov.exception.DatabaseException;
import ru.klokov.model.Currency;
import ru.klokov.model.ExchangeRate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IExchangeRateDAO {
    List<ExchangeRate> findAll() throws DatabaseException;
    Optional<ExchangeRate> findByCurrencyPair(Currency baseCurrency, Currency targetCurrency) throws DatabaseException;
    ExchangeRate save(ExchangeRate exchangeRate) throws DatabaseException;
    ExchangeRate update(ExchangeRate exchangeRate, BigDecimal newRate) throws DatabaseException;
}
