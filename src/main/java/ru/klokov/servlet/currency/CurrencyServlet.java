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
import java.util.Optional;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {
    private ICurrencyDAO currencyDAO;
    private ObjectMapper mapper;

    @Override
    public void init(ServletConfig config) {
        currencyDAO = (CurrencyDAO) config.getServletContext().getAttribute("currencyDAO");
        mapper = (ObjectMapper) config.getServletContext().getAttribute("mapper");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        resp.setContentType("application/json");

        if (pathInfo.isBlank() || pathInfo.substring(1).length() != 3) {
            ResponseHandler.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    mapper.writeValueAsString(new ErrorResponse("Currency code not present in address!")));
            return;
        }

        String currencyCode = pathInfo.substring(1).toUpperCase();

        try {
            Optional<Currency> currency = currencyDAO.findByCode(currencyCode);
            if (currency.isEmpty()) ResponseHandler.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                    mapper.writeValueAsString(new ErrorResponse("Currency with code " + currencyCode + " not found!")));
            else ResponseHandler.sendResponse(resp, HttpServletResponse.SC_OK,
                    mapper.writeValueAsString(currency.get()));
        } catch (DatabaseException e) {
            ResponseHandler.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    mapper.writeValueAsString(new ErrorResponse(e.getMessage())));
        }
    }
}
