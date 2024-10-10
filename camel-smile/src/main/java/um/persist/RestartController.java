package um.persist;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestartController {

    @PostMapping("/stop")
    public void restart() {
        CamelSpringBootApplication.restart();
    }
}