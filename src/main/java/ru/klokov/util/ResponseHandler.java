package ru.klokov.util;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ResponseHandler {
    public static void sendResponse(HttpServletResponse resp, int code, String message) throws IOException {
        resp.setStatus(code);
        resp.getWriter().write(message);
        resp.getWriter().close();
    }
}
