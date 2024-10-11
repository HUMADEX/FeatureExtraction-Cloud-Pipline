package um.persist;

import org.springframework.boot.ApplicationArguments;
import org.springframework.context.ConfigurableApplicationContext;
import um.persist.*;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.ws.rs.core.Application;


@SpringBootApplication
public class CamelSpringBootApplication {

    /**
     *
     * @author UM FERI
     * @date NOV 2020
     * @description A main method to start this application
     * <p/>
     * Use <tt>@SpringBootApplication</tt> to start Camel as Spring Boot application.
     */

    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        context = SpringApplication.run(CamelSpringBootApplication.class, args);
		//new ClassPathXmlApplicationContext("/home/user/my-camel-app/src/main/java/com/example/camel-context.xml");
    }

    public static void restart() {
        ApplicationArguments args = context.getBean(ApplicationArguments.class);

        Thread thread = new Thread(() -> {
            context.close();
            context = SpringApplication.run(CamelSpringBootApplication.class, args.getSourceArgs());
        });

        thread.setDaemon(true);
        thread.start();
    }

}
