package ru.klokov.servlet.currency;

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
import ru.klokov.servlet.BaseServlet;
import ru.klokov.util.ResponseHandler;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/currency/*")
public class CurrencyServlet extends BaseServlet {
    private ICurrencyDAO currencyDAO;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        currencyDAO = (CurrencyDAO) config.getServletContext().getAttribute("currencyDAO");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        resp.setContentType("application/json");

        if (pathInfo.isBlank() || pathInfo.substring(1).length() != 3)
            throw new WrongParametersException("Wrong currency code parameter!");

        String currencyCode = pathInfo.substring(1).toUpperCase();

        Optional<Currency> currency = currencyDAO.findByCode(currencyCode);

        if (currency.isEmpty())
            throw new ResourceNotFoundException("Currency not found with code " + currencyCode);

        ResponseHandler.sendResponse(resp, HttpServletResponse.SC_OK, mapper.writeValueAsString(currency.get()));
    }
}
