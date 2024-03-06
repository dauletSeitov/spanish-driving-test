package spanish.driving.test.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import spanish.driving.test.entity.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DataWriter {

    private final static String PREFIX = "data/";
    @Autowired
    private ObjectMapper objectMapper;

    @SneakyThrows
    public void write(Test test) {
        log.info("question count is {}: ", test.getQuestions().size());
        String content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(test);
        Files.createDirectories(Path.of(PREFIX));
        String fileName = urlToFileName(test.getTestUrl());
        Files.writeString(Path.of(PREFIX, fileName), content);
    }


    public String urlToFileName(String url) {
        return url.replaceAll("/", "-");
    }

    @SneakyThrows
    public Set<String> getAllFiles() {
        if (Files.exists(Path.of(PREFIX))) {
            return Files.list(Path.of(PREFIX))
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString).map(this::urlToFileName)
                    .collect(Collectors.toSet());
        }
        return new HashSet<>();
    }
}
