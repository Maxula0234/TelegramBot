import CurrencyService.CurrencyConversionService;
import CurrencyService.CurrencyModeService;
import Data.Currency;
import Utils.HelpUtils;
import lombok.SneakyThrows;
import lombok.Value;
import org.glassfish.grizzly.utils.StringFilter;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class TgBot extends TelegramLongPollingBot {
    CurrencyModeService currencyModeService = CurrencyModeService.getInstance();
    HelpUtils helpUtils = new HelpUtils(currencyModeService);
    CurrencyConversionService currencyConversionService = CurrencyConversionService.getInstance();
    List<String> logUsers = new LinkedList<>();
    Properties prop = new Properties();
    FileInputStream fis;

    public TgBot(DefaultBotOptions options) {
        super(options);
    }

    public static void main(String[] args) throws TelegramApiException {
        TgBot bot = new TgBot(new DefaultBotOptions());
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(bot);
    }

    public String getBotUsername() {
        return "@TaskSchedulingBot";
    }

    @SneakyThrows
    @Override
    public String getBotToken() {
        loadProperties();
        String token = prop.getProperty("db.tokenBot");
        return token;
    }

    private void loadProperties() throws IOException {
        fis = new FileInputStream("src/main/resources/config.properties");
        prop.load(fis);
    }

    /**
     *
     * Проверяем введеные в чат сообщения
     * Проверяем запросы на обновления
     *
     * @param update
     */
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        }
        if (update.hasMessage()) {
            handleMessage(update.getMessage());
        }
    }

    /**
     * метод, который чекает ответ в чат от пользователя
     * Считываем нажатие кнопок
     * чтобы отрисовать новые - с указателем
     *
     * @param callbackQuery
     */
    @SneakyThrows
    private void handleCallback(CallbackQuery callbackQuery) {
        Message message = callbackQuery.getMessage();
        System.out.println(callbackQuery.getData());
        String[] param = callbackQuery.getData().split(":");
        String action = param[0];
        Currency newCurrency = Currency.valueOf(param[1]);

        switch (action) {
            case "ORIGINAL": {
                currencyModeService.setOriginalCurrency(message.getChatId(), newCurrency);
                break;
            }
            case "TARGET": {
                currencyModeService.setTargetCurrency(message.getChatId(), newCurrency);
                break;
            }
        }

        List<List<InlineKeyboardButton>> buttons = helpUtils.createButtons(message);

        execute(EditMessageReplyMarkup.builder()
                .chatId(message.getChatId().toString())
                .messageId(message.getMessageId())
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                .build()
        );
    }

    /**
     * Контроллер для управления командами
     * Считываем команду
     * Считываем какие кнопки нажаты
     * Проверяем, что введенный текст = число
     * Конвертируем из одной валюты в другую
     *
     * @param message
     * @throws TelegramApiException
     */
    private void handleMessage(Message message) throws TelegramApiException {
        if (message.hasText() && message.hasEntities()) {
            Optional<MessageEntity> commandEntity = message.getEntities().stream().filter(e -> "bot_command".equals(e.getType())).findFirst();
            if (commandEntity.isPresent()) {
                String command = message.getText().substring(commandEntity.get().getOffset(), commandEntity.get().getLength());
                switch (command) {
                    case "/set_currency": {
                        List<List<InlineKeyboardButton>> buttons = helpUtils.createButtons(message);
                        execute(
                                SendMessage.builder()
                                        .text("Необходимо выбрать валюту")
                                        .chatId(message.getChatId().toString())
                                        .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                                        .build()
                        );
                        return;
                    }
                }
            }
        }
        if (message.hasText()) {
            String messageText = message.getText();
            System.out.println("handleMessage - " + messageText);
            Optional<Double> value = helpUtils.parseDouble(messageText);
            System.out.println("userName = " + message.getFrom().getUserName());
            logUsers.add(message.getFrom().getUserName());

            Currency originalCurrency = currencyModeService.getOriginalCurrency(message.getChatId());
            Currency targetCurrency = currencyModeService.getTargetCurrency(message.getChatId());

            double conversionRatio = currencyConversionService.getConversionRatio(originalCurrency, targetCurrency);

            if (value.isPresent()) {
                execute(SendMessage.builder()
                        .chatId(message.getChatId().toString())
                        .text(String.format("%4.2f %s is %4.2f %s", value.get(), originalCurrency, (value.get() * conversionRatio), targetCurrency))
                        .build());
            }
        }
    }


}
