package spanish.driving.test;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

@Slf4j
@EntityScan
@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        log.info("App is starting!!!");
        SpringApplication.run(Main.class, args);
    }
}