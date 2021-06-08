package no.toreb.nrknewsconsumer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Configuration
public class DataJdbcConfig extends AbstractJdbcConfiguration {

    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(List.of(new OffsetDateTimeConverter()));
    }

    @ReadingConverter
    private static class OffsetDateTimeConverter implements Converter<Timestamp, OffsetDateTime> {

        @Override
        public OffsetDateTime convert(final Timestamp timestamp) {
            return timestamp.toLocalDateTime().atOffset(ZoneOffset.UTC);
        }
    }
}
