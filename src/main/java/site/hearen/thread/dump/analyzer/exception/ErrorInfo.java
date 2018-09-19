package site.hearen.thread.dump.analyzer.exception;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorInfo {
    public static final Integer OK = 200;
    public static final Integer UNAUTHORIZED = 401;
    public static final Integer ERROR = 500;

    private Integer status;
    private String message;
    private String url;
    private LocalDateTime timestamp;
}
