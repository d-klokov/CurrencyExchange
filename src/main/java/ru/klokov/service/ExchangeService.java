package ru.klokov.service;

import ru.klokov.dao.ICurrencyDAO;
import ru.klokov.dao.IExchangeRateDAO;
import ru.klokov.exception.DatabaseException;
import ru.klokov.exception.ResourceNotFoundException;
import ru.klokov.model.Currency;
import ru.klokov.model.ExchangeRate;
import ru.klokov.response.ExchangeResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class ExchangeService {
    private static final int SCALE = 4;
    private final ICurrencyDAO currencyDAO;
    private final IExchangeRateDAO exchangeRateDAO;

    public ExchangeService(ICurrencyDAO currencyDAO, IExchangeRateDAO exchangeRateDAO) {
        this.currencyDAO = currencyDAO;
        this.exchangeRateDAO = exchangeRateDAO;
    }

    public ExchangeResponse convert(Currency baseCurrency, Currency targetCurrency, BigDecimal amount) throws DatabaseException, ResourceNotFoundException {
        Optional<ExchangeRate> exchangeRateOptional = getExchangeRate(baseCurrency, targetCurrency);

        if (exchangeRateOptional.isEmpty()) throw new ResourceNotFoundException("Exchange rate with code pair " +
                baseCurrency.getCode() + "-" + targetCurrency.getCode() + " not found!");

        ExchangeRate exchangeRate = exchangeRateOptional.get();
        BigDecimal convertedAmount = amount.multiply(exchangeRate.getRate()).setScale(SCALE, RoundingMode.HALF_EVEN);

        return new ExchangeResponse(
                baseCurrency,
                targetCurrency,
                exchangeRate.getRate(),
                amount,
                convertedAmount
        );
    }

    private Optional<ExchangeRate> getExchangeRate(Currency baseCurrency, Currency targetCurrency) {
        Optional<ExchangeRate> exchangeRate = findDirectRate(baseCurrency, targetCurrency);
        if (exchangeRate.isEmpty()) exchangeRate = findReverseRate(baseCurrency, targetCurrency);
        if (exchangeRate.isEmpty()) exchangeRate = findCrossRate(baseCurrency, targetCurrency);

        return exchangeRate;
    }

    private Optional<ExchangeRate> findDirectRate(Currency baseCurrency, Currency targetCurrency) {
        return exchangeRateDAO.findByCurrencyPair(baseCurrency, targetCurrency);
    }

    private Optional<ExchangeRate> findReverseRate(Currency baseCurrency, Currency targetCurrency) {
        Optional<ExchangeRate> exchangeRateOptional = exchangeRateDAO.findByCurrencyPair(targetCurrency, baseCurrency);
        if (exchangeRateOptional.isPresent()) {
            ExchangeRate exchangeRate = exchangeRateOptional.get();
            BigDecimal rate = BigDecimal.ONE.divide(exchangeRate.getRate(), SCALE, RoundingMode.HALF_EVEN);
            exchangeRate.setRate(rate);
            return Optional.of(exchangeRate);
        }
        return exchangeRateOptional;
    }

    private Optional<ExchangeRate> findCrossRate(Currency baseCurrency, Currency targetCurrency) {
        Optional<Currency> usd = currencyDAO.findByCode("USD");

        if (usd.isEmpty()) throw new ResourceNotFoundException("Currency with code USD not found!");

        Optional<ExchangeRate> usdToBase = exchangeRateDAO.findByCurrencyPair(usd.get(), baseCurrency);
        if (usdToBase.isEmpty()) throw new ResourceNotFoundException("Exchange rate with code pair USD-" +
                baseCurrency.getCode() + " not found!");

        Optional<ExchangeRate> usdToTarget = exchangeRateDAO.findByCurrencyPair(usd.get(), targetCurrency);
        if (usdToTarget.isEmpty()) throw new ResourceNotFoundException("Exchange rate with code pair USD-" +
                targetCurrency.getCode() + " not found!");

        BigDecimal rate = usdToTarget.get().getRate().divide(usdToBase.get().getRate(), SCALE, RoundingMode.HALF_EVEN);

        return Optional.of(new ExchangeRate(baseCurrency.getId(), targetCurrency.getId(), rate));
    }
}
