package br.ufpb.caixacerto.movement;

import br.ufpb.caixacerto.common.enums.MovementNature;
import br.ufpb.caixacerto.common.enums.MovementType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class MovementDTOs {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull(message = "Natureza é obrigatória (ENTRADA ou SAIDA)")
        private MovementNature nature;

        @NotNull(message = "Tipo é obrigatório (NEGOCIO ou PESSOAL)")
        private MovementType type;

        @NotBlank(message = "Categoria é obrigatória")
        @Size(max = 50, message = "Categoria muito longa")
        private String category;

        @NotBlank(message = "Valor é obrigatório")
        private String value; // Será validado e convertido no service

        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 80, message = "Descrição pode ter até 80 caracteres")
        private String description;

        @NotNull(message = "Data é obrigatória")
        private LocalDate date;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private MovementNature nature;
        private MovementType type;

        @Size(max = 50, message = "Categoria muito longa")
        private String category;

        private String value;

        @Size(max = 80, message = "Descrição pode ter até 80 caracteres")
        private String description;

        private LocalDate date;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Integer id;
        private LocalDate date;
        private MovementNature nature;
        private MovementType type;
        private String category;
        private Integer amountCents;
        private String formattedValue;
        private String description;
        private String createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageResponse {
        private List<Response> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterRequest {
        private MovementType type;
        private MovementNature nature;
        private LocalDate startDate;
        private LocalDate endDate;
        private String category;
        private int page = 0;
        private int size = 20;
    }
}