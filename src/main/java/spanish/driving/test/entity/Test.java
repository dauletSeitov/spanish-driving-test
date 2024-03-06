package spanish.driving.test.entity;


import lombok.Data;

import java.util.List;

@Data
public class Test {

    private Long id;
    private String testUrl;

    private List<Question> questions;

    private String lang;

}
