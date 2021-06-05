package no.toreb.nrknewsconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.AbstractEnvironment;

import java.io.IOException;

import static no.toreb.nrknewsconsumer.HsqldbServer.startHsqlDbServer;

@SpringBootApplication
public class ApplicationTestRunner {

    public static void main(final String[] args) throws IOException {
        System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, "localhost");
        startHsqlDbServer();
        SpringApplication.run(ApplicationTestRunner.class, args);
    }
}
