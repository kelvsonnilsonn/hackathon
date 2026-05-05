package com.connectbeleza.connectbeleza.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class PaginacaoUtil {

    @Value("${app.pagination.default-page-size:20}")
    private int defaultPageSize;

    @Value("${app.pagination.max-page-size:100}")
    private int maxPageSize;

    public Pageable build(int page, int size) {
        int tamanho = Math.min(size > 0 ? size : defaultPageSize, maxPageSize);
        return PageRequest.of(Math.max(page, 0), tamanho);
    }

    public Pageable build(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by("desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        int tamanho = Math.min(size > 0 ? size : defaultPageSize, maxPageSize);
        return PageRequest.of(Math.max(page, 0), tamanho, sort);
    }
}