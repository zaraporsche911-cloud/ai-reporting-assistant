package io.github.zaraporsche911cloud.reportingassistant.report.validator;

import io.github.zaraporsche911cloud.reportingassistant.report.model.DateRange;
import io.github.zaraporsche911cloud.reportingassistant.report.model.RelativePeriod;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class DateRangeResolverTest {

    private final DateRangeResolver resolver = new DateRangeResolver(
            Clock.fixed(Instant.parse("2026-07-19T12:00:00Z"), ZoneOffset.UTC));

    @Test
    void resolvesLastMonthAtCalendarBoundaries() {
        DateRange range = resolver.resolve(new DateRange(RelativePeriod.LAST_MONTH, null, null));
        assertThat(range.from()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(range.to()).isEqualTo(LocalDate.of(2026, 6, 30));
    }

    @Test
    void defaultsToCurrentMonth() {
        DateRange range = resolver.resolve(null);
        assertThat(range.from()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(range.to()).isEqualTo(LocalDate.of(2026, 7, 19));
    }
}
