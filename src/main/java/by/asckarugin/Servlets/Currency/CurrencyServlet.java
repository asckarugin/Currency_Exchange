package by.asckarugin.Servlets.Currency;

import by.asckarugin.Model.Currency;
import by.asckarugin.Model.Response.ErrorResponse;
import by.asckarugin.Services.CurrencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {
    private final CurrencyService currencyService = CurrencyService.getInstance();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = req.getPathInfo().replace("/", "").toUpperCase();

        try {
            Optional<Currency> optionalCurrency = currencyService.findByCode(code);

            if(optionalCurrency.isEmpty()){
                resp.setStatus(404);
                objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                        404,
                        "Такой валюты нет в базе данных"
                ));
            }

            objectMapper.writeValue(resp.getWriter(), optionalCurrency.get());

        } catch (SQLException e) {
            resp.setStatus(500);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    500,
                    "Что-то произошло с базами данных, повторите попытку"
            ));
        }
    }
}
