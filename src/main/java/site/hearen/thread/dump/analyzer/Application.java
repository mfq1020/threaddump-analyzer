package site.hearen.thread.dump.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
//        ApplicationContext applicationContext =
        SpringApplication.run(Application.class, args);
//        List<String> beanNames = Arrays.asList(applicationContext.getBeanDefinitionNames());
//        beanNames.stream().sorted().forEach(out::println);
    }
}

