package ru.klokov.servlet.currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.klokov.dao.CurrencyDAO;
import ru.klokov.dao.ICurrencyDAO;
import ru.klokov.exception.DatabaseException;
import ru.klokov.model.Currency;
import ru.klokov.response.ErrorResponse;
import ru.klokov.util.ResponseHandler;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {
    private ICurrencyDAO currencyDAO;
    private ObjectMapper mapper;

    @Override
    public void init(ServletConfig config) {
        currencyDAO = (CurrencyDAO) config.getServletContext().getAttribute("currencyDAO");
        mapper = (ObjectMapper) config.getServletContext().getAttribute("mapper");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        List<Currency> currencies;
        try {
            currencies = currencyDAO.findAll();
            ResponseHandler.sendResponse(resp, HttpServletResponse.SC_OK, mapper.writeValueAsString(currencies));
        } catch (DatabaseException e) {
            ResponseHandler.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    mapper.writeValueAsString(new ErrorResponse(e.getMessage())));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");

        resp.setContentType("application/json");

        if (currencyParamsNotValid(name, code, sign)) {
            ResponseHandler.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    mapper.writeValueAsString(new ErrorResponse("Wrong request parameters!")));
            return;
        }

        try {
            Optional<Currency> currency = currencyDAO.findByCode(code);
            if (currency.isPresent()) ResponseHandler.sendResponse(resp, HttpServletResponse.SC_CONFLICT,
                    mapper.writeValueAsString(new ErrorResponse("Currency with code " + code + " already exists!")));
            else {
                Currency createdCurrency = currencyDAO.save(new Currency(code, name, sign));
                ResponseHandler.sendResponse(resp, HttpServletResponse.SC_OK, mapper.writeValueAsString(createdCurrency));
            }
        } catch (DatabaseException e) {
            ResponseHandler.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    mapper.writeValueAsString(new ErrorResponse(e.getMessage())));
        }
    }

    private boolean currencyParamsNotValid(String name, String code, String sign) {
        return (name.isBlank() || name.length() > 50 ||
                code.isBlank() || code.length() != 3 || sign.isBlank() || sign.length() > 5);
    }
}
