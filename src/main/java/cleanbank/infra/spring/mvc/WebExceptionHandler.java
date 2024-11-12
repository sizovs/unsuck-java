package cleanbank.infra.spring.mvc;

import cleanbank.infra.pipeline.RateLimiting;
import cleanbank.infra.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@ControllerAdvice
public class WebExceptionHandler {

  @ExceptionHandler(value = Validator.ValidationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public @ResponseBody List<String> handle(Validator.ValidationException ex) {
    return ex.violations();
  }

  @ExceptionHandler(value = RateLimiting.RateLimitException.class)
  @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
  public @ResponseBody List<String> handle(RateLimiting.RateLimitException ex) {
    return List.of(ex.getMessage());
  }
}
