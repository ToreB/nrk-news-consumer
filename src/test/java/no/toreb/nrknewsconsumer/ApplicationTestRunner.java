package no.toreb.nrknewsconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.AbstractEnvironment;

@SpringBootApplication
public class ApplicationTestRunner {

    public static void main(final String[] args) {
        System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, "localhost");
        SpringApplication.run(ApplicationTestRunner.class, args);
    }
}
