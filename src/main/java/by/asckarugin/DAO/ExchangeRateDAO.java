package by.asckarugin.DAO;

import by.asckarugin.Model.ExchangeRate;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateDAO extends CrudDAO<ExchangeRate>{
    List<ExchangeRate> findCodeWithUsdBase(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;

    Optional<ExchangeRate> findByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;
}
