package io.github.zaraporsche911cloud.reportingassistant.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PageRequestFactory {

    public Pageable create(int page, int size, String sortBy, Sort.Direction direction, Set<String> allowedSorts) {
        if (page < 0) throw new IllegalArgumentException("page must not be negative");
        if (size < 1 || size > 100) throw new IllegalArgumentException("size must be between 1 and 100");
        if (!allowedSorts.contains(sortBy)) throw new IllegalArgumentException("Unsupported sort field: " + sortBy);
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
