package io.github.zaraporsche911cloud.reportingassistant.report.executor;

import io.github.zaraporsche911cloud.reportingassistant.exception.UnsupportedReportException;
import io.github.zaraporsche911cloud.reportingassistant.report.model.ReportType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ReportExecutorRegistry {

    private final Map<ReportType, ReportExecutor> executors = new EnumMap<>(ReportType.class);

    public ReportExecutorRegistry(List<ReportExecutor> executorBeans) {
        executorBeans.forEach(executor -> executor.supportedTypes().forEach(type -> {
            ReportExecutor previous = executors.put(type, executor);
            if (previous != null) throw new IllegalStateException("Duplicate report executor for " + type);
        }));
    }

    public ReportExecutor get(ReportType type) {
        ReportExecutor executor = executors.get(type);
        if (executor == null) throw new UnsupportedReportException("No executor is registered for " + type);
        return executor;
    }
}
