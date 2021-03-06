package net.es.oscars.pss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableConfigurationProperties
@EnableAsync
@EnableScheduling
@ComponentScan({"net.es.oscars.pss","net.es.oscars.rest"})
public class PssApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext app = SpringApplication.run(PssApp.class, args);

    }

}
