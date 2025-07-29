package io.github.pavelshe11.authmicro.api.http.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldErrorDto {
    private String field;
    private String message;
}
