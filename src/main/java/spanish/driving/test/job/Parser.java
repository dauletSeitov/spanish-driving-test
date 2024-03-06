package spanish.driving.test.job;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import spanish.driving.test.bot.TelegramBotOutput;
import spanish.driving.test.entity.Option;
import spanish.driving.test.entity.Question;
import spanish.driving.test.entity.Test;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class Parser {


    @Value("${app.selenium.chrome.url}")
    private String seleniumChromeUrl;

    @Autowired
    private DataWriter dataWriter;

    @Autowired
    private TelegramBotOutput telegramBotOutput;

    @Value("${app.principal.id}")
    private String principalId;
    static boolean testMode = false;

    private Map<String, String> urlMap = Map.of(
            "en_TEST_LIST_PAGE", "https://practicatest.com/en/spanish-driving-test",
            "en_LOGIN_PAGE", "https://practicatest.com/en/spanish-driving-test#modalLogin",
            "en_FAVORITE", "https://practicatest.com/en/favorite-test",
            "es_TEST_LIST_PAGE", "https://practicatest.com/tests/permiso-B",
            "es_LOGIN_PAGE", "https://practicatest.com/tests/permiso-B#modalLogin",
            "es_FAVORITE", "https://practicatest.com/home/test-favoritos");

    enum URLType {
        FAVORITE,
        LOGIN_PAGE,
        TEST_LIST_PAGE
    }

    private String getURL(String lang, URLType type) {
        return urlMap.get(lang + "_" + type);
    }

    @SneakyThrows
    public void parse(String lang) {

        ChromeOptions options = new ChromeOptions();

//        WebDriver driver = new ChromeDriver(options);
        WebDriver driver = new RemoteWebDriver(new URL(seleniumChromeUrl), options);

        driver.get(getURL(lang, URLType.LOGIN_PAGE));

        WebElement loginField = findElement(driver, "//*[@id='ms-login-form-email']");
        loginField.sendKeys("silov.skynet@gmail.com");

        WebElement passwordField = findElement(driver, "//*[@id='ms-form-pass']");
        passwordField.sendKeys("fX$Q5j5MT7qN4-U");

        WebElement loginSubmitButton = findElement(driver, "//*[@id='button-login-submit']");
        loginSubmitButton.click();

        Set<String> allFiles = dataWriter.getAllFiles();

        int tests = testMode ? 1 : 40;
        for (int i = 0; i < tests; i++) {
            try {
                Optional<Test> testOpt = handleRow(driver, i, allFiles, lang);
                if (testOpt.isPresent()) {
                    Test test = testOpt.get();
                    dataWriter.write(test);


                    String message = "" + LocalDate.now() + "\n" +
                            "test parsed: " + testOpt.get().getTestUrl();
                    SendMessage msg = new SendMessage();
                    msg.setChatId(principalId);
                    msg.setText(message);
                    telegramBotOutput.execute(msg);
                    log.info(message);
                }
            } catch (Exception ex) {
                SendMessage msg = new SendMessage();
                msg.setChatId(principalId);
                msg.setText(Arrays.toString(ex.getStackTrace()));
                telegramBotOutput.execute(msg);
                log.error(ex.getMessage(), ex);
            }
        }

        System.out.println();
        driver.quit();
    }

    public Optional<Test> handleRow(WebDriver driver, int index, Set<String> importedTestUrls, String lang) {
        sleep(5);
        driver.get(getURL(lang, URLType.TEST_LIST_PAGE));

        WebElement listOfExams = findElement(driver, "//*[@id='id_test_dia']");
        listOfExams.click();


        WebElement table = findElement(driver, "//*[@id=\"box-list\"]/table/tbody");


        List<WebElement> a = table.findElements(By.tagName("a"));
        WebElement webElement = a.get(index);
        if (!"TEST".equals(webElement.getText())) {
            return Optional.empty();
        }
        String testRef = webElement.getAttribute("href");

        if (importedTestUrls.contains(dataWriter.urlToFileName(testRef))) {
            log.info("url {} already loaded", testRef);
            return Optional.empty();
        }

        webElement.click();

        sleep(5);


        System.out.println("---------------------" + index + "------------------------");

        Test test = new Test();
        test.setTestUrl(testRef);
        test.setQuestions(handlePage(driver, lang));
        test.setLang(lang);
        return Optional.of(test);
    }

    private List<Question> handlePage(WebDriver driver, String lang) {

        WebElement element = findElement(driver, "//*[@id=\"select_test_mode\"]/div/div/div[2]/div/div[1]/a");
        element.click();

        List<Question> questions = new ArrayList<>();

        int questionsSize = testMode ? 1 : 30;
        for (int i = 0; i < questionsSize; i++) {
            Question question = new Question();
            question.setParseDate(LocalDateTime.now());
            WebElement questionDiv = findElementById(driver, "section" + (i + 1));
            WebElement img = findElementByTagName(questionDiv, "img");

            String src = img.getAttribute("src");
            System.out.println(src);
            question.setImageUrl(src);
            WebElement ul = findElementByTagName(questionDiv, "ul");
            WebElement optionA = findElementByTagName(ul, "a");
            optionA.click();


            WebElement btn = findElementById(questionDiv, "pregunta_step_" + (i + 1));
            btn.click();


            sleep(1);
            try {
                WebElement close = driver.findElement(By.xpath("//*[@id=\"myModal\"]/div/div/div[1]/button"));
                close.click();
            } catch (Exception ex) {

            }


            WebElement ui = findElementByTagName(questionDiv, "ul");
            List<WebElement> options = findElementsByTagName(ui, "li");
            WebElement h4 = findElementByTagName(questionDiv, "h4");

            System.out.println(i + 1 + ") ");
            System.out.println(questionDiv.getText());////////////

            question.setQuestion(h4.getText());

            List<Option> list = options.stream().map(itm -> {
                WebElement a = findElementByTagName(itm, "a");
                boolean contains = itm.getAttribute("class").contains("passq");
                return new Option(a.getText(), contains);
            }).toList();

            question.setOptions(list);

            WebElement favoriteBtn = findElementById(questionDiv, "set-favourite-" + (i + 1));
            favoriteBtn.click();
            goToFavorite(driver, question, lang);
            questions.add(question);
        }
        return questions;
    }

    private void goToFavorite(WebDriver driver, Question question, String lang) {

        ((JavascriptExecutor) driver).executeScript("window.open()");
        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(1));
        driver.get(getURL(lang, URLType.FAVORITE));
        sleep(2);
        WebElement panel = findElementById(driver, "panel_1");
        panel = findElementByTagName(panel, "a");
        panel.click();

        WebElement div = findElementById(driver, "preguntale_1");

        String explanation = div.getText();
        System.out.println("explanation = " + explanation);
        question.setExplanation(explanation);

        WebElement remove = findElementById(driver, "quest_1");
        remove = findElementByTagName(remove, "a");
        remove.click();

        driver.close();
        driver.switchTo().window(tabs.get(0));

    }

    public static WebElement findElementByTagName(SearchContext driver, String tagName) {
        sleep(1);
        WebElement element = driver.findElement(By.tagName(tagName));
        sleep(1);
        return element;
    }

    public static List<WebElement> findElementsByTagName(SearchContext driver, String tagName) {
        sleep(1);
        List<WebElement> lis = driver.findElements(By.tagName(tagName));
        sleep(1);
        return lis;
    }

    public static WebElement findElementById(SearchContext driver, String id) {
        sleep(1);
        WebElement element = driver.findElement(By.id(id));
        sleep(1);
        return element;
    }

    public static WebElement findElement(WebDriver driver, String xpath) {
        sleep(1);
        WebElement element = driver.findElement(By.xpath(xpath));
        sleep(1);
        return element;
    }

    public static WebElement findElement(WebDriver driver, String xpath, int val) {
        sleep(val);
        WebElement element = driver.findElement(By.xpath(xpath));
        sleep(val);
        return element;
    }

    public static void sleep(int val) {
        try {
            Thread.sleep(val * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
