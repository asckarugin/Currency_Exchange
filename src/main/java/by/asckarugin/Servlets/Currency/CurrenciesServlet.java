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
import java.util.List;

@WebServlet(urlPatterns = "/currencies")
public class CurrenciesServlet extends HttpServlet {
    private final CurrencyService currencyService = CurrencyService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            List<Currency> currencyList = currencyService.showAllCurrencies();
            objectMapper.writeValue(resp.getWriter(), currencyList);
        } catch (SQLException e){
            resp.setStatus(500);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    500,
                    "Что-то произошло с базами данных, повторите попытку"
            ));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");

        if(name.isEmpty()){
            resp.setStatus(400);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    400,
                    "Отсутсвует параметр - имя"
            ));
        }

        if(code.isEmpty()){
            resp.setStatus(400);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    400,
                    "Отсутсвует параметр - код"
            ));
        }

        if(sign.isEmpty()){
            resp.setStatus(400);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    400,
                    "Отсутствует параметр - символ валюты"
            ));
        }


        try {
            Currency currency = new Currency(code, name, sign);
            currencyService.saveCurrency(currency);

            objectMapper.writeValue(resp.getWriter(), currency);

        } catch (SQLException e) {
            if(e.getSQLState().equals("23505")){
                resp.setStatus(409);
                objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                        409,
                        "Такая валюта уже существует"
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
