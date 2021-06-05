package no.toreb.nrknewsconsumer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(classes = NrkNewsConsumerApplication.class,
                properties = {
                        "spring.datasource.url=jdbc:hsqldb:mem:testdb;sql.syntax_pgs=true",
                        "spring.datasource.username=sa"
                })
class NrkNewsConsumerApplicationTests {

    @MockBean
    private BuildProperties buildProperties;

    @Test
    void contextLoads() {
    }

}
