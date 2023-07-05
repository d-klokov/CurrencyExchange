package ru.klokov.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.klokov.dao.CurrencyDAO;
import ru.klokov.dao.ICurrencyDAO;
import ru.klokov.exception.ResourceNotFoundException;
import ru.klokov.exception.WrongParametersException;
import ru.klokov.model.Currency;
import ru.klokov.service.ExchangeService;
import ru.klokov.util.ResponseHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@WebServlet("/exchange")
public class ExchangeServlet extends BaseServlet {
    private ICurrencyDAO currencyDAO;
    private ExchangeService exchangeService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        currencyDAO = (CurrencyDAO) config.getServletContext().getAttribute("currencyDAO");
        exchangeService = (ExchangeService) config.getServletContext().getAttribute("exchangeService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String fromCode = req.getParameter("from");
        String toCode = req.getParameter("to");
        String amountParam = req.getParameter("amount");

        resp.setContentType("application/json");

        if (exchangeParamsNotValid(fromCode, toCode, amountParam))
            throw new WrongParametersException("Wrong currency codes or amount parameters!");

        BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(amountParam));

        Optional<Currency> from = currencyDAO.findByCode(fromCode);
        Optional<Currency> to = currencyDAO.findByCode(toCode);

        if (from.isEmpty()) throw new ResourceNotFoundException("Currency with code " + fromCode + " not found");
        if (to.isEmpty()) throw new ResourceNotFoundException("Currency with code " + toCode + " not found");

        Currency fromCurrency = from.get();
        Currency toCurrency = to.get();

        ResponseHandler.sendResponse(resp, HttpServletResponse.SC_OK, mapper.writeValueAsString(exchangeService.convert(fromCurrency, toCurrency, amount)));
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

