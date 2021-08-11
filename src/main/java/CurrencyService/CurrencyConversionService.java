package CurrencyService;

import CurrencyService.impl.NbrbCurrencyConversionService;
import Data.Currency;

public interface CurrencyConversionService {
    static CurrencyConversionService getInstance(){
        return new NbrbCurrencyConversionService();
    }

    double getConversionRatio(Currency original, Currency target);
}
