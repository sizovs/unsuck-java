package cleanbank.infra.spring.mvc;

import cleanbank.infra.pipeline.RateLimited;
import cleanbank.infra.validation.Rules;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@ControllerAdvice
public class WebExceptionHandler {

  @ExceptionHandler(value = Rules.Violations.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public @ResponseBody List<String> handle(Rules.Violations ex) {
    return ex.violations();
  }

  @ExceptionHandler(value = RateLimited.TooManyRequests.class)
  @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
  public @ResponseBody List<String> handle(RateLimited.TooManyRequests ex) {
    return List.of(ex.getMessage());
  }
}
