package net.es.oscars;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.export_ts.ExportTimestamps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import java.util.Arrays;

@Configuration
@EnableAutoConfiguration
@ComponentScan(
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = Backend.class),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "net.es.oscars.topo.pop.*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "net.es.oscars.topo.svc.*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "net.es.oscars.security.*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "net.es.oscars.resv.svc.*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "net.es.oscars.pss.svc.*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "net.es.oscars.app.*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "net.es.oscars.soap.*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "net.es.oscars.pce.*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "net.es.oscars.nsi.*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "net.es.oscars.ext.*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "net.es.oscars.task.*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "net.es.oscars.web.*"),

        }
)
@Slf4j
public class ExportApp {
    public static void main(String[] args) {
        SpringApplication.run(ExportApp.class, args);

    }


    @Autowired
    private ExportTimestamps engine;



    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
/*
            System.out.println("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }
            */
            this.engine.export();
            System.exit(0);

        };
    }

}