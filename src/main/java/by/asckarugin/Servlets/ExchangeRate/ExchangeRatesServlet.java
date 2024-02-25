package by.asckarugin.Servlets.ExchangeRate;

import by.asckarugin.Model.ExchangeRate;
import by.asckarugin.Model.Response.ErrorResponse;
import by.asckarugin.Services.CurrencyService;
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
import java.util.List;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private final ExchangeService exchangeService = ExchangeService.getInstance();
    private final CurrencyService currencyService = CurrencyService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<ExchangeRate> exchangeRate = exchangeService.findAllExchange();
            objectMapper.writeValue(resp.getWriter(), exchangeRate);
        } catch (SQLException e) {
            resp.setStatus(500);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    500,
                    "Что-то произошло с базами данных, повторите попытку"
            ));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String rateString = req.getParameter("rate");

        if(baseCurrencyCode.isEmpty()){
            resp.setStatus(400);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    400,
                    "Отстутствует нужный параметр - baseCurrencyCode"
            ));
        }

        if(targetCurrencyCode.isEmpty()){
            resp.setStatus(400);
            objectMapper.writeValue(resp.getWriter(),new ErrorResponse(
                    400,
                    "Отстутствует нужный параметр - targetCurrencyCode"
            ));
        }

        if(rateString.isEmpty()){
            resp.setStatus(400);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    400,
                    "Отстутствует нужный параметр - rate"
            ));
        }

        BigDecimal rate = BigDecimal.valueOf(Double.parseDouble(rateString));


        try {
            ExchangeRate exchangeRate = new ExchangeRate(
                    currencyService.findByCode(baseCurrencyCode).orElseThrow(),
                    currencyService.findByCode(targetCurrencyCode).orElseThrow(),
                    rate
            );
            exchangeService.saveExchangeRates(exchangeRate);

            objectMapper.writeValue(resp.getWriter(), exchangeRate);

        } catch (SQLException e) {
            if(e.getSQLState().equals("23505")){
                resp.setStatus(409);
                objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                        409,
                        e.getMessage()
                ));
            }
            if(e.getErrorCode()==404){
                resp.setStatus(404);
                objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                        404,
                        e.getMessage()
                ));
            }
            resp.setStatus(500);
            objectMapper.writeValue(resp.getWriter(),new ErrorResponse(
                    500,
                    "Что-то произошло с базами данных, повторите попытку"
            ));
        }
    }
}
