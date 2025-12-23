package com.ticketpark.ticketpark.ticket.dto;

import org.springframework.data.domain.Page;

import java.util.List;
public record PageResponseDTO<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PageResponseDTO<T> from(Page<T> p) {
        return new PageResponseDTO<>(
                p.getContent(),
                p.getNumber() + 1,
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages(),
                p.isLast()
        );
    }
}