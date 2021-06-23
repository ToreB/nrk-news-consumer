package no.toreb.nrknewsconsumer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.relational.core.dialect.AnsiDialect;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Configuration
public class DataJdbcConfig extends AbstractJdbcConfiguration {

    @Override
    public Dialect jdbcDialect(final NamedParameterJdbcOperations operations) {
        return AnsiDialect.INSTANCE;
    }

    @Override
    public JdbcMappingContext jdbcMappingContext(final Optional<NamingStrategy> namingStrategy,
                                                 final JdbcCustomConversions customConversions) {
        final JdbcMappingContext jdbcMappingContext = super.jdbcMappingContext(namingStrategy, customConversions);
        jdbcMappingContext.setForceQuote(false);
        return jdbcMappingContext;
    }

    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(List.of(new StringToOffsetDateTimeConverter(),
                                                 new OffsetDateTimeToStringConverter()));
    }

    @ReadingConverter
    private static class StringToOffsetDateTimeConverter implements Converter<String, OffsetDateTime> {

        @Override
        public OffsetDateTime convert(final String source) {
            return OffsetDateTime.parse(source);
        }
    }

    @WritingConverter
    private static class OffsetDateTimeToStringConverter implements Converter<OffsetDateTime, String> {

        @Override
        public String convert(final OffsetDateTime source) {
            return source.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
    }

}
