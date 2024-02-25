package by.asckarugin.Servlets.ExchangeRate;

import by.asckarugin.Model.Response.ErrorResponse;
import by.asckarugin.Model.Response.ExchangeResponse;
import by.asckarugin.Services.ExchangeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {

    private final ExchangeService exchangeService = ExchangeService.getInstance();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String baseCurrencyCode = req.getParameter("from");
        String targetCurrencyCode = req.getParameter("to");
        String amountString = req.getParameter("amount");

        if(baseCurrencyCode.isEmpty()){
            resp.setStatus(400);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    400,
                    "Отсутствует параметр - from"
            ));
        }

        if(targetCurrencyCode.isEmpty()){
            resp.setStatus(400);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    400,
                    "Отсутствует параметр - to"
            ));
        }

        if(amountString.isEmpty()){
            resp.setStatus(400);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    400,
                    "Отсутствует параметр - amount"
            ));
        }

        BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(amountString));
        try {
            ExchangeResponse exchangeResponse = exchangeService.convertCurrency(baseCurrencyCode, targetCurrencyCode, amount);
            objectMapper.writeValue(resp.getWriter(),exchangeResponse);

        } catch (SQLException e) {
            resp.setStatus(500);
            objectMapper.writeValue(resp.getWriter(),new ErrorResponse(
                    500,
                    "Что-то произошло с базами данных, повторите попытку"
            ));
        }
    }
}
