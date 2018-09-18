package site.hearen.threaddump;

import static java.lang.System.out;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        ApplicationContext applicationContext =
                SpringApplication.run(Application.class, args);
        List<String> beanNames = Arrays.asList(applicationContext.getBeanDefinitionNames());
        beanNames.stream().sorted().forEach(out::println);
    }
}

