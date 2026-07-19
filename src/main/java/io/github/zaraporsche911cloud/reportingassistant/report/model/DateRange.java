package io.github.zaraporsche911cloud.reportingassistant.report.model;

import java.time.LocalDate;

public record DateRange(RelativePeriod type, LocalDate from, LocalDate to) {

    public static DateRange relative(RelativePeriod period) {
        return new DateRange(period, null, null);
    }

    public static DateRange custom(LocalDate from, LocalDate to) {
        return new DateRange(RelativePeriod.CUSTOM, from, to);
    }
}
