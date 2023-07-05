package ru.klokov.servlet.exchangeRate;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.klokov.dao.CurrencyDAO;
import ru.klokov.dao.ExchangeRateDAO;
import ru.klokov.dao.ICurrencyDAO;
import ru.klokov.dao.IExchangeRateDAO;
import ru.klokov.exception.ResourceNotFoundException;
import ru.klokov.exception.WrongParametersException;
import ru.klokov.model.Currency;
import ru.klokov.model.ExchangeRate;
import ru.klokov.response.ExchangeRateResponse;
import ru.klokov.servlet.BaseServlet;
import ru.klokov.util.ResponseHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends BaseServlet {
    private ICurrencyDAO currencyDAO;
    private IExchangeRateDAO exchangeRateDAO;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        currencyDAO = (CurrencyDAO) config.getServletContext().getAttribute("currencyDAO");
        exchangeRateDAO = (ExchangeRateDAO) config.getServletContext().getAttribute("exchangeRateDAO");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        resp.setContentType("application/json");

        if (pathInfo.isBlank() || pathInfo.substring(1).length() != 6)
            throw new WrongParametersException("Wrong currency codes or rate parameters!");

        String baseCurrencyCode = pathInfo.substring(1, 4).toUpperCase();
        String targetCurrencyCode = pathInfo.substring(4).toUpperCase();

        Optional<Currency> baseCurrency = currencyDAO.findByCode(baseCurrencyCode);
        Optional<Currency> targetCurrency = currencyDAO.findByCode(targetCurrencyCode);

        if (baseCurrency.isEmpty())
            throw new ResourceNotFoundException("Currency with code " + baseCurrencyCode + " not found!");
        if (targetCurrency.isEmpty())
            throw new ResourceNotFoundException("Currency with code " + targetCurrencyCode + " not found!");

        Optional<ExchangeRate> exchangeRate = exchangeRateDAO.findByCurrencyPair(baseCurrency.get(), targetCurrency.get());

        if (exchangeRate.isEmpty())
            throw new ResourceNotFoundException("Exchange rate with code pair " + baseCurrencyCode + "-" + targetCurrencyCode + " not found!");

        ExchangeRate rate = exchangeRate.get();
        ExchangeRateResponse exchangeRateResponse = new ExchangeRateResponse(
                rate.getId(),
                baseCurrency.get(),
                targetCurrency.get(),
                rate.getRate()
        );

        ResponseHandler.sendResponse(resp, HttpServletResponse.SC_OK, mapper.writeValueAsString(exchangeRateResponse));
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        String rate = getRateFromRequest(req).substring(5);

        resp.setContentType("application/json");

        if (pathInfo.isBlank() || pathInfo.substring(1).length() != 6)
            throw new WrongParametersException("Currency codes not present in address!");

        if (!rateParameterIsDouble(rate))
            throw new WrongParametersException("Rate parameter value must be a number!");

        String baseCurrencyCode = pathInfo.substring(1, 4).toUpperCase();
        String targetCurrencyCode = pathInfo.substring(4).toUpperCase();

        Optional<Currency> baseCurrency = currencyDAO.findByCode(baseCurrencyCode);
        Optional<Currency> targetCurrency = currencyDAO.findByCode(targetCurrencyCode);

        if (baseCurrency.isEmpty())
            throw new ResourceNotFoundException("Currency with code " + baseCurrencyCode + " not found!");
        if (targetCurrency.isEmpty())
            throw new ResourceNotFoundException("Currency with code " + targetCurrencyCode + " not found!");

        Optional<ExchangeRate> exchangeRate = exchangeRateDAO.findByCurrencyPair(baseCurrency.get(), targetCurrency.get());

        if (exchangeRate.isEmpty())
            throw new ResourceNotFoundException("Exchange rate with code pair " + baseCurrencyCode + "-" + targetCurrencyCode + " not found!");

        ExchangeRate updatedExchangeRate = exchangeRateDAO.update(exchangeRate.get(), BigDecimal.valueOf(Double.parseDouble(rate)));

        ExchangeRateResponse exchangeRateResponse = new ExchangeRateResponse(
                updatedExchangeRate.getId(),
                baseCurrency.get(),
                targetCurrency.get(),
                updatedExchangeRate.getRate()
        );

        ResponseHandler.sendResponse(resp, HttpServletResponse.SC_OK, mapper.writeValueAsString(exchangeRateResponse));
    }

    private String getRateFromRequest(HttpServletRequest req) {
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(req.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bufferedReader.lines().collect(Collectors.joining("\n"));
    }

    private boolean rateParameterIsDouble(String rate) {
        try {
            Double.parseDouble(rate);
        }
        catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
