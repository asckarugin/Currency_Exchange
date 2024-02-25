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
import java.util.Optional;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private final ExchangeService exchangeService = ExchangeService.getInstance();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(req.getMethod().equalsIgnoreCase("PATCH")){
            this.doPatch(req, resp);
        } else{
            super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getPathInfo().replaceAll("/","");

        if(url.length()!=6){
            resp.setStatus(400);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    400,
                    "Валюты введенны в неправильном формате"
            ));
        }

        String baseCurrencyCode = url.substring(0,3);
        String targetCurrencyCode = url.substring(3);

        try {
            Optional<ExchangeRate> exchangeRate = exchangeService.findByCodes(baseCurrencyCode, targetCurrencyCode);

            if(exchangeRate.isEmpty()){
                resp.setStatus(404);
                objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                        404,
                        "Для этой валютной пары не существует обменного курса"
                ));
            }

            objectMapper.writeValue(resp.getWriter(), exchangeRate.get());
        } catch (SQLException e) {
            resp.setStatus(500);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    500,
                    "Что-то произошло с базами данных, повторите попытку"
            ));
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String url = req.getPathInfo().replaceAll("/","");
        String rateString = req.getParameter("rate");

        if(rateString.isEmpty()){
            resp.setStatus(400);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    400,
                    "Отсутствует параметр - rate"
            ));
        }

        if(url.length()!=6){
            resp.setStatus(404);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    404,
                    "Валюты введенны в неправильном формате"
            ));
        }

        String baseCurrencyCode = url.substring(0,3);
        String targetCurrencyCode = url.substring(3);
        BigDecimal rate = BigDecimal.valueOf(Double.parseDouble(rateString));

        try {
            Optional<ExchangeRate> exchangeRateOptional = exchangeService.findByCodes(baseCurrencyCode, targetCurrencyCode);

            if(exchangeRateOptional.isEmpty()){
                resp.setStatus(404);
                objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                        404,
                        "Для этой валютной пары не существует обменного курса"
                ));
            }

            ExchangeRate exchangeRate = exchangeRateOptional.get();
            exchangeRate.setRate(rate);
            exchangeService.updateExchangeRates(exchangeRate);

            objectMapper.writeValue(resp.getWriter(), exchangeRate);
        } catch (SQLException e) {
            resp.setStatus(500);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    500,
                    "Что-то произошло с базами данных, повторите попытку"
            ));
        }
    }
}
