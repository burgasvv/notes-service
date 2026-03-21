package org.burgas.javaspring.dto.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.burgas.javaspring.dto.Response;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionResponse implements Response {

    private String status;
    private Integer code;
    private String message;
}
