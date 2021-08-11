package Utils;

import CurrencyService.CurrencyModeService;
import Data.Currency;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class HelpUtils {

    private CurrencyModeService currencyModeService;
    public HelpUtils(CurrencyModeService currencyModeService){
        this.currencyModeService = currencyModeService;
    }

    public  Optional<Double> parseDouble(String messageText) {
        try {
            return Optional.of(Double.parseDouble(messageText));
        } catch (Exception e) {
            System.out.println("parseDouble - false");
            return Optional.empty();
        }
    }


    public  String getCurrencyButton(Currency saved, Currency current) {
        return saved == current ? current + " ✅" : current.name();
    }


    public  List<List<InlineKeyboardButton>> createButtons(Message message) {

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        Currency originalCurrency = currencyModeService.getOriginalCurrency(message.getChatId());
        Currency targetCurrency = currencyModeService.getTargetCurrency(message.getChatId());

        for (Currency currency : Currency.values()) {
            buttons.add(Arrays.asList(
                    InlineKeyboardButton.builder()
                            .text(getCurrencyButton(originalCurrency, currency))
                            .callbackData("ORIGINAL:" + currency)
                            .build(),
                    InlineKeyboardButton.builder()
                            .text(getCurrencyButton(targetCurrency, currency))
                            .callbackData("TARGET:" + currency)
                            .build()));
        }
        return buttons;
    }

}
