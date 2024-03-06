package spanish.driving.test.entity;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Question {

    private String question;
    private List<Option> options;
    private String explanation;
    private String imageUrl;
    private LocalDateTime parseDate;

}
