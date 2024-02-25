package by.asckarugin.DAO.Impl;

import by.asckarugin.DAO.CrudDAO;
import by.asckarugin.DAO.CurrencyDAO;
import by.asckarugin.Model.Currency;
import by.asckarugin.Utils.ConnectionManager;
import lombok.Getter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class CurrencyDAOImpl implements CurrencyDAO {

    private static final CurrencyDAOImpl INSTANCE = new CurrencyDAOImpl();

    private CurrencyDAOImpl(){}

    @Override
    public Optional<Currency> findById(Long id) throws SQLException {
        final String query ="""
            SELECT *
            FROM currencies
            WHERE id = ?
            """;

        try(Connection connection = ConnectionManager.get();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            Currency currency = null;
            if(resultSet.next()){
                currency = buildCurrency(resultSet);
            }
            return Optional.ofNullable(currency);
        }
    }

    @Override
    public List<Currency> findAll() throws SQLException {
        final String query = """
                SELECT *
                FROM currencies
                """;

        try(Connection connection = ConnectionManager.get();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            List<Currency> currencies = new ArrayList<>();
            while(resultSet.next()){
                currencies.add(buildCurrency(resultSet));
            }
            return currencies;
        }
    }

    @Override
    public Currency save(Currency currency) throws SQLException {
        final String query= """
                INSERT INTO currencies(code, fullname, sign)
                VALUES(?,?,?);
                """;

        try(Connection connection = ConnectionManager.get();
            PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, currency.getCode());
            preparedStatement.setString(2, currency.getFullName());
            preparedStatement.setString(3, currency.getSign());

            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if(generatedKeys.next()){
                currency.setId(generatedKeys.getLong("id"));
            }
            return currency;
        }
    }

    @Override
    public void update(Currency currency) throws SQLException {
        final String query= """
                UPDATE currencies
                SET code = ?,
                    fullname = ?,
                    sign = ?
                WHERE id = ?
                """;

        try(Connection connection = ConnectionManager.get();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, currency.getCode());
            preparedStatement.setString(2, currency.getFullName());
            preparedStatement.setString(3, currency.getSign());
            preparedStatement.setLong(4, currency.getId());

            preparedStatement.executeQuery();
        }
    }

    @Override
    public Optional<Currency> findByCode(String code) throws SQLException {
        String query= """
                SELECT *
                FROM currencies
                WHERE code = ?
                """;

        try(Connection connection = ConnectionManager.get();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1,code);
            ResultSet resultSet = preparedStatement.executeQuery();

            Currency currency = null;
            if(resultSet.next()){
                currency = buildCurrency(resultSet);
            }
            return Optional.ofNullable(currency);
        }
    }

    private Currency buildCurrency(ResultSet resultSet) throws SQLException {
        return new Currency(
                resultSet.getLong("id"),
                resultSet.getString("code"),
                resultSet.getString("fullName"),
                resultSet.getString("sign")
        );
    }

    public static CurrencyDAOImpl getInstance(){
        return INSTANCE;
    }

}
