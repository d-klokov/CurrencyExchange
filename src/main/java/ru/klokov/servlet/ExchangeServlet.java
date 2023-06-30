package ru.klokov.servlet;

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
import ru.klokov.response.ExchangeResponse;
import ru.klokov.util.ResponseHandler;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {
    private ObjectMapper mapper;
    private ICurrencyDAO currencyDAO;
    private IExchangeRateDAO exchangeRateDAO;

    @Override
    public void init(ServletConfig config) {
        mapper = (ObjectMapper) config.getServletContext().getAttribute("mapper");
        currencyDAO = (CurrencyDAO) config.getServletContext().getAttribute("currencyDAO");
        exchangeRateDAO = (ExchangeRateDAO) config.getServletContext().getAttribute("exchangeRateDAO");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String fromCode = req.getParameter("from");
        String toCode = req.getParameter("to");
        String amount = req.getParameter("amount");

        double convertedAmount, rate;

        resp.setContentType("application/json");

        if (exchangeParamsNotValid(fromCode, toCode, amount)) {
            ResponseHandler.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    mapper.writeValueAsString(new ErrorResponse("Wrong request parameters!")));
            return;
        }

        double amountDouble = Double.parseDouble(amount);

        try {
            Optional<Currency> from = currencyDAO.findByCode(fromCode);
            Optional<Currency> to = currencyDAO.findByCode(toCode);

            if (from.isEmpty()) {
                ResponseHandler.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        mapper.writeValueAsString(new ErrorResponse("Currency with code " + fromCode + " not found")));
                return;
            }

            if (to.isEmpty()) {
                ResponseHandler.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                        mapper.writeValueAsString(new ErrorResponse("Currency with code " + toCode + " not found")));
                return;
            }

            Currency fromCurrency = from.get();
            Currency toCurrency = to.get();

            Optional<ExchangeRate> exchangeRate = exchangeRateDAO.findByCurrencyPair(fromCurrency, toCurrency);

            if (exchangeRate.isPresent()) {
                rate = exchangeRate.get().getRate();
            } else {
                exchangeRate = exchangeRateDAO.findByCurrencyPair(toCurrency, fromCurrency);
                if (exchangeRate.isPresent()) {
                    rate = 1 / exchangeRate.get().getRate();
                } else {
                    Optional<Currency> usd = currencyDAO.findByCode("USD");
                    if (usd.isEmpty()) {
                        ResponseHandler.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                                mapper.writeValueAsString(new ErrorResponse("Currency with code USD not found!")));
                        return;
                    }

                    Currency usdCurrency = usd.get();
                    Optional<ExchangeRate> crossExchangeRateFrom = exchangeRateDAO.findByCurrencyPair(usdCurrency, fromCurrency);

                    if (crossExchangeRateFrom.isEmpty()) {
                        ResponseHandler.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                                mapper.writeValueAsString(new ErrorResponse("Exchange rate with code pair USD-" + fromCode + " not found!")));
                        return;
                    }

                    Optional<ExchangeRate> crossExchangeRateTo = exchangeRateDAO.findByCurrencyPair(usdCurrency, toCurrency);

                    if (crossExchangeRateTo.isEmpty()) {
                        ResponseHandler.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                                mapper.writeValueAsString(new ErrorResponse("Exchange rate with code pair USD-" + toCode + " not found!")));
                        return;
                    }

                    rate = crossExchangeRateFrom.get().getRate() / crossExchangeRateTo.get().getRate();
                }
            }
            convertedAmount = amountDouble * rate;

            ResponseHandler.sendResponse(resp, HttpServletResponse.SC_OK,
                    mapper.writeValueAsString(new ExchangeResponse(
                            fromCurrency,
                            toCurrency,
                            rate,
                            amountDouble,
                            convertedAmount
                    )));
        } catch (DatabaseException e) {
            ResponseHandler.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    mapper.writeValueAsString(new ErrorResponse(e.getMessage())));
        }
    }

    private boolean exchangeParamsNotValid(String from, String to, String amount) {
        return from.isBlank() || from.length() != 3 || to.isBlank() || to.length() != 3 ||
                amount.isBlank() || !amountParameterIsDouble(amount);
    }

    private boolean amountParameterIsDouble(String amount) {
        try {
            Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}

