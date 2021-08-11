package CurrencyService.impl;

import CurrencyService.*;
import Data.Currency;

import java.util.HashMap;
import java.util.Map;

public class HashMapCurrencyModeService implements CurrencyModeService {

    private final Map<Long, Currency> originalCurrency = new HashMap<>();
    private final Map<Long, Currency> targetCurrency = new HashMap<>();

    public HashMapCurrencyModeService(){
        System.out.println("HASHMAP MODE is created");
    }
    @Override
    public Currency getOriginalCurrency(long chatId) {
        return originalCurrency.getOrDefault(chatId, Currency.USD);
    }

    @Override
    public Currency getTargetCurrency(long chatId) {
        return targetCurrency.getOrDefault(chatId, Currency.USD);
    }

    public void setTargetCurrency(long chatId, Currency currency) {
        targetCurrency.put(chatId,currency);
    }

    public void setOriginalCurrency(long chatId, Currency currency) {
        originalCurrency.put(chatId,currency);
    }
}
