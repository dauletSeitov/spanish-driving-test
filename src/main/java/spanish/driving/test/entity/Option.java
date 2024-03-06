package spanish.driving.test.entity;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Option {

    private String option;
    private boolean isCorrect;

}
