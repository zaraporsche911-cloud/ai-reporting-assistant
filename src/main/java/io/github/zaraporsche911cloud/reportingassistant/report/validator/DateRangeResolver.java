package io.github.zaraporsche911cloud.reportingassistant.report.validator;

import io.github.zaraporsche911cloud.reportingassistant.report.model.DateRange;
import io.github.zaraporsche911cloud.reportingassistant.report.model.RelativePeriod;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;

@Component
public class DateRangeResolver {

    private final Clock clock;

    public DateRangeResolver(Clock clock) {
        this.clock = clock;
    }

    public DateRange resolve(DateRange requested) {
        RelativePeriod period = requested == null || requested.type() == null
                ? RelativePeriod.THIS_MONTH
                : requested.type();
        LocalDate today = LocalDate.now(clock);
        return switch (period) {
            case TODAY -> DateRange.custom(today, today);
            case YESTERDAY -> DateRange.custom(today.minusDays(1), today.minusDays(1));
            case THIS_WEEK -> DateRange.custom(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), today);
            case LAST_WEEK -> {
                LocalDate end = today.with(TemporalAdjusters.previous(DayOfWeek.MONDAY)).minusDays(1);
                yield DateRange.custom(end.minusDays(6), end);
            }
            case THIS_MONTH -> DateRange.custom(today.withDayOfMonth(1), today);
            case LAST_MONTH -> {
                LocalDate month = today.minusMonths(1);
                yield DateRange.custom(month.withDayOfMonth(1), month.with(TemporalAdjusters.lastDayOfMonth()));
            }
            case THIS_QUARTER -> DateRange.custom(quarterStart(today), today);
            case LAST_QUARTER -> {
                LocalDate prior = quarterStart(today).minusDays(1);
                yield DateRange.custom(quarterStart(prior), prior);
            }
            case THIS_YEAR -> DateRange.custom(today.withDayOfYear(1), today);
            case LAST_YEAR -> DateRange.custom(LocalDate.of(today.getYear() - 1, Month.JANUARY, 1),
                    LocalDate.of(today.getYear() - 1, Month.DECEMBER, 31));
            case LAST_7_DAYS -> DateRange.custom(today.minusDays(6), today);
            case LAST_30_DAYS -> DateRange.custom(today.minusDays(29), today);
            case LAST_90_DAYS -> DateRange.custom(today.minusDays(89), today);
            case CUSTOM -> DateRange.custom(requested.from(), requested.to());
        };
    }

    private LocalDate quarterStart(LocalDate date) {
        int firstMonth = ((date.getMonthValue() - 1) / 3) * 3 + 1;
        return LocalDate.of(date.getYear(), firstMonth, 1);
    }
}
