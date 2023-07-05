package ru.klokov.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.klokov.exception.DatabaseException;
import ru.klokov.exception.ResourceAlreadyExistsException;
import ru.klokov.exception.ResourceNotFoundException;
import ru.klokov.exception.WrongParametersException;
import ru.klokov.response.ErrorResponse;
import ru.klokov.util.ResponseHandler;

import java.io.IOException;

public class BaseServlet extends HttpServlet {
    protected ObjectMapper mapper;
    @Override
    public void init(ServletConfig config) throws ServletException {
        mapper = (ObjectMapper) config.getServletContext().getAttribute("mapper");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (req.getMethod().equals("PATCH")) {
                doPatch(req, resp);
            } else super.service(req, resp);
        } catch (DatabaseException|ServletException e) {
            ResponseHandler.sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    mapper.writeValueAsString(new ErrorResponse(e.getMessage())));
        } catch (ResourceNotFoundException e) {
            ResponseHandler.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND,
                    mapper.writeValueAsString(new ErrorResponse(e.getMessage())));
        } catch (ResourceAlreadyExistsException e) {
            ResponseHandler.sendResponse(resp, HttpServletResponse.SC_CONFLICT,
                    mapper.writeValueAsString(new ErrorResponse(e.getMessage())));
        } catch (WrongParametersException e) {
            ResponseHandler.sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST,
                    mapper.writeValueAsString(new ErrorResponse(e.getMessage())));
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {}
}
