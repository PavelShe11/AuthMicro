package io.github.pavelshe11.authmicro.api.http.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDto {
    private String error;
    private List<FieldErrorDto> detailedErrors;
}