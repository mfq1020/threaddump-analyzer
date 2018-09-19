package site.hearen.thread.dump.analyzer;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import site.hearen.thread.dump.analyzer.exception.MyException;
import site.hearen.thread.dump.analyzer.exception.ErrorInfo;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ErrorInfo unknownExceptionHandler(HttpServletRequest req, MyException e) throws Exception {
        return ErrorInfo.builder()
                .message(e.getMessage())
                .status(ErrorInfo.ERROR)
                .url(req.getRequestURL().toString())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
