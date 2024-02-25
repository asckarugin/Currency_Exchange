package by.asckarugin.DAO.Impl;

import by.asckarugin.DAO.ExchangeRateDAO;
import by.asckarugin.Model.ExchangeRate;
import by.asckarugin.Utils.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDAOImpl implements ExchangeRateDAO {

    private static final ExchangeRateDAOImpl INSTANCE = new ExchangeRateDAOImpl();

    private final CurrencyDAOImpl currencyDAO = CurrencyDAOImpl.getInstance();

    private ExchangeRateDAOImpl(){}

    @Override
    public Optional<ExchangeRate> findById(Long id) {
        String query = """
                SELECT *
                FROM exchange_rates
                WHERE id = ?
                """;

        try(Connection connection = ConnectionManager.get();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1,id);

            ResultSet resultSet = preparedStatement.executeQuery();
            ExchangeRate exchangeRate = null;
            if(resultSet.next()){
                exchangeRate = buildExchangeRate(resultSet);
            }
            return Optional.ofNullable(exchangeRate);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public List<ExchangeRate> findAll() throws SQLException {
        String query = """
                SELECT *
                FROM exchange_rates
                """;

        try(Connection connection = ConnectionManager.get();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            List<ExchangeRate> exchangeRates = new ArrayList<>();
            while(resultSet.next()){
                exchangeRates.add(buildExchangeRate(resultSet));
            }
            return exchangeRates;
        }
    }

    @Override
    public ExchangeRate save(ExchangeRate exchangeRate) throws SQLException {
        String query = """
                INSERT INTO exchange_rates(base_currency_id, target_currency_id, rate) 
                VALUES (?,?,?) 
                """;

        try(Connection connection = ConnectionManager.get();
            PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setLong(1, exchangeRate.getBaseCurrencyId().getId());
            preparedStatement.setLong(2, exchangeRate.getTargetCurrencyId().getId());
            preparedStatement.setBigDecimal(3, exchangeRate.getRate());

            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if(generatedKeys.next()){
                exchangeRate.setId(generatedKeys.getLong("id"));
            }
            return exchangeRate;
        }
    }

    @Override
    public void update(ExchangeRate exchangeRate) throws SQLException {
        String query= """
                UPDATE exchange_rates
                SET base_currency_id = ?,
                    target_currency_id = ?,
                    rate = ?
                WHERE id = ?
                """;

        try(Connection connection = ConnectionManager.get();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, exchangeRate.getBaseCurrencyId().getId());
            preparedStatement.setLong(2, exchangeRate.getTargetCurrencyId().getId());
            preparedStatement.setBigDecimal(3, exchangeRate.getRate());
            preparedStatement.setLong(4, exchangeRate.getId());

            preparedStatement.executeUpdate();
        }
    }

    @Override
    public List<ExchangeRate> findCodeWithUsdBase(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        final String query = """
                SELECT *
                FROM exchange_rates
                JOIN currencies bc ON exchange_rates.base_currency_id = bc.id
                JOIN currencies tc ON exchange_rates.target_currency_id = tc.id
                WHERE(
                    base_currency_id = (SELECT c.id FROM currencies c WHERE c.code = 'USD'),
                    target_currency_id = (SELECT c2.id FROM currencies c2 WHERE c2.code = ?),
                    target_currency_id = (SELECT c3.id FROM currencies c3 WHERE c3.code = ?)
                )
                """;

        try(Connection connection = ConnectionManager.get();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, baseCurrencyCode);
            preparedStatement.setString(2, targetCurrencyCode);

            ResultSet resultSet = preparedStatement.executeQuery();
            List<ExchangeRate> exchangeRates = new ArrayList<>();
            if(resultSet.next()){
                exchangeRates.add(buildExchangeRate(resultSet));
            }
            return exchangeRates;
        }
    }

    @Override
    public Optional<ExchangeRate> findByCode(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        final String query= """
                SELECT *
                FROM exchange_rates
                JOIN currencies bc ON exchange_rates.base_currency_id = bc.id
                JOIN currencies tc ON exchange_rates.target_currency_id = tc.id
                WHERE(
                    base_currency_id = (SELECT c.id FROM currencies c WHERE c.code = ?) AND
                    target_currency_id = (SELECT c2.id FROM currencies c2 WHERE c2.code = ?)
                )
                """;

        try(Connection connection = ConnectionManager.get();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, baseCurrencyCode);
            preparedStatement.setString(2, targetCurrencyCode);
            ResultSet resultSet = preparedStatement.executeQuery();

            ExchangeRate exchangeRates = null;
            if(resultSet.next()){
                exchangeRates = buildExchangeRate(resultSet);
            }
            return Optional.ofNullable(exchangeRates);
        }
    }

    private ExchangeRate buildExchangeRate(ResultSet resultSet) throws SQLException {
        return new ExchangeRate(
            resultSet.getLong("id"),
            currencyDAO.findById(resultSet.getLong("base_currency_id")).orElse(null),
            currencyDAO.findById(resultSet.getLong("target_currency_id")).orElse(null),
            resultSet.getBigDecimal("rate")
        );
    }

    public static ExchangeRateDAOImpl getInstance(){
        return INSTANCE;
    }

}
