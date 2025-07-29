package io.github.pavelshe11.authmicro.api.http.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeErrorDto {
    private String error;
    private String type ;
}
