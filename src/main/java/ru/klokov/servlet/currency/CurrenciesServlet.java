package ru.klokov.servlet.currency;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.klokov.dao.CurrencyDAO;
import ru.klokov.dao.ICurrencyDAO;
import ru.klokov.exception.ResourceAlreadyExistsException;
import ru.klokov.exception.WrongParametersException;
import ru.klokov.model.Currency;
import ru.klokov.servlet.BaseServlet;
import ru.klokov.util.ResponseHandler;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet("/currencies")
public class CurrenciesServlet extends BaseServlet {
    private ICurrencyDAO currencyDAO;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        currencyDAO = (CurrencyDAO) config.getServletContext().getAttribute("currencyDAO");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        List<Currency>currencies = currencyDAO.findAll();

        ResponseHandler.sendResponse(resp, HttpServletResponse.SC_OK, mapper.writeValueAsString(currencies));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");

        resp.setContentType("application/json");

        if (currencyParamsNotValid(name, code, sign))
            throw new WrongParametersException("Wrong name, code or sign parameters!");

        Optional<Currency> currency = currencyDAO.findByCode(code);

        if (currency.isPresent())
            throw new ResourceAlreadyExistsException("Currency with code " + code + " already exists!");

        Currency createdCurrency = currencyDAO.save(new Currency(code, name, sign));

        ResponseHandler.sendResponse(resp, HttpServletResponse.SC_OK, mapper.writeValueAsString(createdCurrency));
    }

    private boolean currencyParamsNotValid(String name, String code, String sign) {
        return (name.isBlank() || name.length() > 50 || code.isBlank() || code.length() != 3 ||
                sign.isBlank() || sign.length() > 5);
    }
}
