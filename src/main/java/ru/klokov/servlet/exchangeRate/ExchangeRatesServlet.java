package ru.klokov.servlet.exchangeRate;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.klokov.dao.CurrencyDAO;
import ru.klokov.dao.ExchangeRateDAO;
import ru.klokov.dao.ICurrencyDAO;
import ru.klokov.dao.IExchangeRateDAO;
import ru.klokov.exception.DatabaseException;
import ru.klokov.model.Currency;
import ru.klokov.model.ExchangeRate;
import ru.klokov.response.ErrorResponse;
import ru.klokov.response.ExchangeRateResponse;
import ru.klokov.util.ResponseHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private ICurrencyDAO currencyDAO;
    private IExchangeRateDAO exchangeRateDAO;
    private ObjectMapper mapper;

    @Override
    public void init(ServletConfig config) {
        currencyDAO = (CurrencyDAO) config.getServletContext().getAttribute("currencyDAO");
        exchangeRateDAO = (ExchangeRateDAO) config.getServletContext().getAttribute("exchangeRateDAO");
        mapper = (ObjectMapper) config.getServletContext().getAttribute("mapper");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        try {
            List<ExchangeRate> exchangeRates = exchangeRateDAO.findAll();
            List<ExchangeRateResponse> exchangeRateResponses = new ArrayList<>();

            for (ExchangeRate exchangeRate : exchangeRates) {
                Optional<Currency> baseCurrency = currencyDAO.findById(exchangeRate.getBaseCurrencyId());
                Optional<Currency> targetCurrency = currencyDAO.findById(exchangeRate.getTargetCurrencyId());

                if (baseCurrency.isEmpty()) {
                    ResponseHandler.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                            mapper.writeValueAsString(new ErrorResponse("Currency with id " + exchangeRate.getBaseCurrencyId() + " not found!")));
                    return;
                }

                if (targetCurrency.isEmpty()) {
                    ResponseHandler.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                            mapper.writeValueAsString(new ErrorResponse("Currency with id " + exchangeRate.getTargetCurrencyId() + " not found!")));
                    return;
                }

                ExchangeRateResponse exchangeRateResponse = new ExchangeRateResponse(
                        exchangeRate.getId(),
                        baseCurrency.get(),
                        targetCurrency.get(),
                        exchangeRate.getRate()
                );

                exchangeRateResponses.add(exchangeRateResponse);
            }

            ResponseHandler.sendResponse(resp, HttpServletResponse.SC_OK, mapper.writeValueAsString(exchangeRateResponses));
        } catch (DatabaseException e) {
            ResponseHandler.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    mapper.writeValueAsString(new ErrorResponse(e.getMessage())));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String rate = req.getParameter("rate");

        resp.setContentType("application/json");

        if (exchangeRateParamsNotValid(baseCurrencyCode, targetCurrencyCode, rate)) {
            ResponseHandler.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    mapper.writeValueAsString(new ErrorResponse("Wrong request parameters!")));
            return;
        }

        try {
            Optional<Currency> baseCurrency = currencyDAO.findByCode(baseCurrencyCode);
            Optional<Currency> targetCurrency = currencyDAO.findByCode(targetCurrencyCode);

            if (baseCurrency.isEmpty()) {
                ResponseHandler.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        mapper.writeValueAsString(new ErrorResponse("Currency with code " + baseCurrencyCode + " not found!")));
                return;
            }

            if (targetCurrency.isEmpty()) {
                ResponseHandler.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        mapper.writeValueAsString(new ErrorResponse("Currency with code " + targetCurrencyCode + " not found!")));
                return;
            }

            Optional<ExchangeRate> exchangeRate = exchangeRateDAO.findByCurrencyPair(baseCurrency.get(), targetCurrency.get());

            if (exchangeRate.isPresent()) {
                ResponseHandler.sendResponse(resp, HttpServletResponse.SC_CONFLICT,
                        mapper.writeValueAsString(new ErrorResponse("Currency pair with code " +
                                baseCurrencyCode + "-" + targetCurrencyCode + " already exists!")));
            } else {
                ExchangeRate createdExchangeRate = exchangeRateDAO.save(new ExchangeRate(
                        baseCurrency.get().getId(),
                        targetCurrency.get().getId(),
                        Double.parseDouble(rate)
                ));

                ExchangeRateResponse exchangeRateResponse = new ExchangeRateResponse(
                        createdExchangeRate.getId(),
                        baseCurrency.get(),
                        targetCurrency.get(),
                        createdExchangeRate.getRate()
                );

                ResponseHandler.sendResponse(resp, HttpServletResponse.SC_OK, mapper.writeValueAsString(exchangeRateResponse));
            }
        } catch (DatabaseException e) {
            ResponseHandler.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    mapper.writeValueAsString(new ErrorResponse(e.getMessage())));
        }
    }

    private boolean exchangeRateParamsNotValid(String baseCurrencyCode, String targetCurrencyCode, String rate) {
        return  (baseCurrencyCode.isBlank() || baseCurrencyCode.length() != 3 ||
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
