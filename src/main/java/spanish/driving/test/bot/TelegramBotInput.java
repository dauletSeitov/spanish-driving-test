package spanish.driving.test.bot;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import spanish.driving.test.job.Job;
import spanish.driving.test.service.CommonService;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBotInput extends TelegramLongPollingBot {

    @Value("${bot.username}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Autowired
    private Job job;

    @Autowired
    private CommonService service;

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        log.info(update.toString());
        if (!update.hasMessage()) {
            return;
        }
        if (update.getMessage().getText().isBlank()) {
            return;
        }
        try {

            String text = update.getMessage().getText();
            if (text.equals("/scrap")) {
                CompletableFuture.runAsync(() -> job.scheduleFixedRateTask());
            } else if (text.equals("/list")) {
                service.getTestList();
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}