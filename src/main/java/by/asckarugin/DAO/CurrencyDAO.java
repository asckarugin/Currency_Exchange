package by.asckarugin.DAO;

import by.asckarugin.Model.Currency;

import java.sql.SQLException;
import java.util.Optional;

public interface CurrencyDAO extends CrudDAO<Currency>{
    Optional<Currency> findByCode(String code) throws SQLException;
}
