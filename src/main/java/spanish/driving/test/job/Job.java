package spanish.driving.test.job;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
public class Job {
    @Autowired
    private Parser parser;

    @SneakyThrows
    @Scheduled(cron = "${app.cron.expression}")
    public void scheduleFixedRateTask() {
        parse("es");
        parse("en");
    }

    @SneakyThrows
    private void parse(String lang) {
        log.info("job started ...");
        try {
            parser.parse(lang);
            log.info("job finished ...");
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}
