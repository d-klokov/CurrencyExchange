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
import ru.klokov.exception.ResourceAlreadyExistsException;
import ru.klokov.exception.ResourceNotFoundException;
import ru.klokov.exception.WrongParametersException;
import ru.klokov.model.Currency;
import ru.klokov.model.ExchangeRate;
import ru.klokov.response.ExchangeRateResponse;
import ru.klokov.servlet.BaseServlet;
import ru.klokov.util.ResponseHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends BaseServlet {
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
        resp.setContentType("application/json");

        List<ExchangeRate> exchangeRates = exchangeRateDAO.findAll();
        List<ExchangeRateResponse> exchangeRateResponses = new ArrayList<>();

        for (ExchangeRate exchangeRate : exchangeRates) {
            Optional<Currency> baseCurrency = currencyDAO.findById(exchangeRate.getBaseCurrencyId());
            Optional<Currency> targetCurrency = currencyDAO.findById(exchangeRate.getTargetCurrencyId());

            if (baseCurrency.isEmpty())
                throw new ResourceNotFoundException("Currency with id " + exchangeRate.getBaseCurrencyId() + " not found!");
            if (targetCurrency.isEmpty())
                throw new ResourceNotFoundException("Currency with id " + exchangeRate.getTargetCurrencyId() + " not found!");

            ExchangeRateResponse exchangeRateResponse = new ExchangeRateResponse(
                    exchangeRate.getId(),
                    baseCurrency.get(),
                    targetCurrency.get(),
                    exchangeRate.getRate()
            );

            exchangeRateResponses.add(exchangeRateResponse);
        }

        ResponseHandler.sendResponse(resp, HttpServletResponse.SC_OK, mapper.writeValueAsString(exchangeRateResponses));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String rate = req.getParameter("rate");

        resp.setContentType("application/json");

        if (exchangeRateParamsNotValid(baseCurrencyCode, targetCurrencyCode, rate))
            throw new WrongParametersException("Wrong currency codes or rate parameters!");

        Optional<Currency> baseCurrency = currencyDAO.findByCode(baseCurrencyCode);
        Optional<Currency> targetCurrency = currencyDAO.findByCode(targetCurrencyCode);

        if (baseCurrency.isEmpty())
            throw new ResourceNotFoundException("Currency with code " + baseCurrencyCode + " not found!");
        if (targetCurrency.isEmpty())
            throw new ResourceNotFoundException("Currency with code " + targetCurrencyCode + " not found!");

        Optional<ExchangeRate> exchangeRate = exchangeRateDAO.findByCurrencyPair(baseCurrency.get(), targetCurrency.get());

        if (exchangeRate.isPresent())
            throw new ResourceAlreadyExistsException("Currency pair with code " + baseCurrencyCode + "-" + targetCurrencyCode + " already exists!");

        ExchangeRate createdExchangeRate = exchangeRateDAO.save(new ExchangeRate(
                baseCurrency.get().getId(),
                targetCurrency.get().getId(),
                BigDecimal.valueOf(Double.parseDouble(rate))
        ));

        ExchangeRateResponse exchangeRateResponse = new ExchangeRateResponse(
                createdExchangeRate.getId(),
                baseCurrency.get(),
                targetCurrency.get(),
                createdExchangeRate.getRate()
        );

        ResponseHandler.sendResponse(resp, HttpServletResponse.SC_OK, mapper.writeValueAsString(exchangeRateResponse));
    }

    private boolean exchangeRateParamsNotValid(String baseCurrencyCode, String targetCurrencyCode, String rate) {
        return (baseCurrencyCode.isBlank() || baseCurrencyCode.length() != 3 ||
            targetCurrencyCode.isBlank() || targetCurrencyCode.length() != 3 ||
            rate.isBlank() || !rateParameterIsDouble(rate));
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
