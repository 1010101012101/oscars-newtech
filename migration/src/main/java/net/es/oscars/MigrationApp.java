package net.es.oscars;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.migration.MigrationEngine;
import net.es.oscars.timefix.Export;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@Slf4j
public class MigrationApp implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(MigrationApp.class, args);

    }

    @Autowired
    private MigrationEngine engine;
    @Autowired
    private Export exporter;

    @Override
    public void run(String... strings) throws Exception {
        this.exporter.export();
//        this.engine.runEngine();
        System.exit(0);

    }
}
