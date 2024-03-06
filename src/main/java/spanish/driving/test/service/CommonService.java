package spanish.driving.test.service;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import spanish.driving.test.bot.TelegramBotOutput;
import spanish.driving.test.job.DataWriter;

import java.util.Set;

@Service
public class CommonService {

    @Autowired
    private TelegramBotOutput telegramBotOutput;
    @Autowired
    private DataWriter dataWriter;
    @Value("${app.principal.id}")
    private String principalId;

    @SneakyThrows
    public void getTestList() {

        Set<String> allFiles = dataWriter.getAllFiles();
        SendMessage document = new SendMessage();
        document.setChatId(principalId);
        document.setText(allFiles.size() + "\n" + String.join("\n", allFiles));
        telegramBotOutput.execute(document);
    }
}
