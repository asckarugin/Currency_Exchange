package by.asckarugin.Services;

import by.asckarugin.DAO.Impl.ExchangeRateDAOImpl;
import by.asckarugin.Model.ExchangeRate;
import by.asckarugin.Model.Response.ExchangeResponse;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ExchangeService {

    private static final ExchangeService INSTANCE = new ExchangeService();

    private ExchangeService(){}
    private final ExchangeRateDAOImpl exchangeRateDAO = ExchangeRateDAOImpl.getInstance();

    public Optional<ExchangeRate> findByIdExchange(Long id){
        return exchangeRateDAO.findById(id);
    }

    public List<ExchangeRate> findAllExchange() throws SQLException {
        return exchangeRateDAO.findAll();
    }

    public void saveExchangeRates(ExchangeRate exchangeRate) throws SQLException {
        exchangeRateDAO.save(exchangeRate);
    }

    public void updateExchangeRates(ExchangeRate exchangeRate) throws SQLException {
        exchangeRateDAO.update(exchangeRate);
    }

    public Optional<ExchangeRate> findByCodes(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        return exchangeRateDAO.findByCode(baseCurrencyCode, targetCurrencyCode);
    }

    public ExchangeResponse convertCurrency(String baseCurrencyCode, String targetCurrencyCode, BigDecimal amount) throws SQLException {
        ExchangeRate exchangeRate = getExchangeRate(baseCurrencyCode, targetCurrencyCode).orElseThrow();

        BigDecimal convertedAmount = amount.multiply(exchangeRate.getRate()).setScale(2, RoundingMode.HALF_EVEN);

        return new ExchangeResponse(
                exchangeRate.getBaseCurrencyId(),
                exchangeRate.getTargetCurrencyId(),
                exchangeRate.getRate(),
                amount,
                convertedAmount
        );
    }

    private Optional<ExchangeRate> getExchangeRate(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        Optional<ExchangeRate> exchangeRate = getDirectExchangeRate(baseCurrencyCode, targetCurrencyCode);

        if(exchangeRate.isEmpty()){
            exchangeRate = getReverseExchangeRate(baseCurrencyCode, targetCurrencyCode);
        }

        if(exchangeRate.isEmpty()){
            exchangeRate = getCrossExchangeRate(baseCurrencyCode, targetCurrencyCode);
        }

        return exchangeRate;
    }

    private Optional<ExchangeRate> getCrossExchangeRate(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        List<ExchangeRate> exchangeRate = exchangeRateDAO.findCodeWithUsdBase(baseCurrencyCode, targetCurrencyCode);

        ExchangeRate usdBaseCurrency = getExchangeInCode(exchangeRate, baseCurrencyCode);
        ExchangeRate usdTargetCurrency = getExchangeInCode(exchangeRate, targetCurrencyCode);

        BigDecimal usdBaseRate = usdBaseCurrency.getRate();
        BigDecimal usdBaseTarget = usdTargetCurrency.getRate();

        BigDecimal baseToTarget = usdBaseTarget.divide(usdBaseRate, MathContext.DECIMAL64);

        return Optional.of(new ExchangeRate(
                usdBaseCurrency.getTargetCurrencyId(),
                usdTargetCurrency.getTargetCurrencyId(),
                baseToTarget
        ));
    }

    private Optional<ExchangeRate> getReverseExchangeRate(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        Optional<ExchangeRate> exchangeRate = exchangeRateDAO.findByCode(targetCurrencyCode, baseCurrencyCode);

        if(exchangeRate.isEmpty()){
            return Optional.empty();
        }

        ExchangeRate reverseExchangeRates = exchangeRate.get();

        return Optional.of(new ExchangeRate(
                reverseExchangeRates.getTargetCurrencyId(),
                reverseExchangeRates.getBaseCurrencyId(),
                BigDecimal.ONE.divide(reverseExchangeRates.getRate(), MathContext.DECIMAL64)
        ));
    }

    private Optional<ExchangeRate> getDirectExchangeRate(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        return exchangeRateDAO.findByCode(baseCurrencyCode, targetCurrencyCode);
    }

    private static ExchangeRate getExchangeInCode(List<ExchangeRate> rates, String code){
        return rates.stream()
                .filter(rate -> rate.getTargetCurrencyId().getCode().equals(code))
                .findFirst()
                .orElseThrow();
    }

    public static ExchangeService getInstance(){
        return INSTANCE;
    }
}
