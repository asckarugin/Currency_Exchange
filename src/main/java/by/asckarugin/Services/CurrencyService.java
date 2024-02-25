package by.asckarugin.Services;

import by.asckarugin.DAO.Impl.CurrencyDAOImpl;
import by.asckarugin.Model.Currency;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CurrencyService {
    private static final CurrencyService INSTANCE = new CurrencyService();
    private CurrencyService(){}
    private final CurrencyDAOImpl currencyDAO = CurrencyDAOImpl.getInstance();

    public Optional<Currency> showByIdCurrency(Long id) throws SQLException {
        return currencyDAO.findById(id);
    }

    public List<Currency> showAllCurrencies() throws SQLException {
        return currencyDAO.findAll();
    }

    public void saveCurrency(Currency currency) throws SQLException {
        currencyDAO.save(currency);
    }

    public void updateCurrency(Currency currency) throws SQLException {
        currencyDAO.update(currency);
    }

    public Optional<Currency> findByCode(String code) throws SQLException {
        return currencyDAO.findByCode(code);
    }

    public static CurrencyService getInstance(){
        return INSTANCE;
    }
}
