package ru.klokov.servlet.exchangeRate;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.stream.Collectors;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
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
        String pathInfo = req.getPathInfo();

        resp.setContentType("application/json");

        if (pathInfo.isBlank() || pathInfo.substring(1).length() != 6) {
            ResponseHandler.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    mapper.writeValueAsString(new ErrorResponse("Currency codes not present in address!")));
            return;
        }

        String baseCurrencyCode = pathInfo.substring(1, 4).toUpperCase();
        String targetCurrencyCode = pathInfo.substring(4).toUpperCase();

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

            if (exchangeRate.isEmpty()) {
                ResponseHandler.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND, mapper.writeValueAsString(
                        new ErrorResponse("Exchange rate with code pair " + baseCurrencyCode + "-" +
                                targetCurrencyCode + " not found!")));
            } else {
                ExchangeRate rate = exchangeRate.get();
                ExchangeRateResponse exchangeRateResponse = new ExchangeRateResponse(
                        rate.getId(),
                        baseCurrency.get(),
                        targetCurrency.get(),
                        rate.getRate()
                );

                ResponseHandler.sendResponse(resp, HttpServletResponse.SC_OK,
                        mapper.writeValueAsString(exchangeRateResponse));
            }
        } catch (DatabaseException e) {
            ResponseHandler.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    mapper.writeValueAsString(new ErrorResponse(e.getMessage())));
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getMethod().equals("PATCH")) {
            doPatch(req, resp);
        } else super.service(req, resp);
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        String rate = getRateFromRequest(req).substring(5);

        resp.setContentType("application/json");

        if (pathInfo.isBlank() || pathInfo.substring(1).length() != 6) {
            ResponseHandler.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    mapper.writeValueAsString(new ErrorResponse("Wrong request parameters!")));
            return;
        }

        if (!rateParameterIsDouble(rate)) {
            ResponseHandler.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    mapper.writeValueAsString(new ErrorResponse("Rate parameter value must be a number!")));
            return;
        }

        String baseCurrencyCode = pathInfo.substring(1, 4).toUpperCase();
        String targetCurrencyCode = pathInfo.substring(4).toUpperCase();

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

            if (exchangeRate.isEmpty()) {
                ResponseHandler.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND, mapper.writeValueAsString(
                        new ErrorResponse("Exchange rate with code pair " + baseCurrencyCode + "-" +
                                targetCurrencyCode + " not found!")));
            } else {
                ExchangeRate updatedExchangeRate = exchangeRateDAO.update(exchangeRate.get(), Double.parseDouble(rate));

                ExchangeRateResponse exchangeRateResponse = new ExchangeRateResponse(
                        updatedExchangeRate.getId(),
                        baseCurrency.get(),
                        targetCurrency.get(),
                        updatedExchangeRate.getRate()
                );

                ResponseHandler.sendResponse(resp, HttpServletResponse.SC_OK, mapper.writeValueAsString(exchangeRateResponse));
            }
        } catch (DatabaseException e) {
            ResponseHandler.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    mapper.writeValueAsString(new ErrorResponse(e.getMessage())));
        }
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
