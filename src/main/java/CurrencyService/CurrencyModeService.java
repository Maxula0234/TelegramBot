package CurrencyService;

import CurrencyService.impl.HashMapCurrencyModeService;
import Data.Currency;

public interface CurrencyModeService {
    static HashMapCurrencyModeService getInstance() {
        return new HashMapCurrencyModeService();
    }

    Currency getOriginalCurrency(long chatId);

    Currency getTargetCurrency(long chatId);

    void setOriginalCurrency(long chatId, Currency currency);

    void setTargetCurrency(long chatId, Currency currency);

}
