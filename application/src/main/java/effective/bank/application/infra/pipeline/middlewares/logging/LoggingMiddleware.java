package effective.bank.application.infra.pipeline.middlewares.logging;

import an.awesome.pipelinr.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(5)
class LoggingMiddleware implements Command.Middleware {

  private final CorrelationId correlationId;

  public LoggingMiddleware(CorrelationId correlationId) {
    this.correlationId = correlationId;
  }

  @Override
  public <R, C extends Command<R>> R invoke(C command, Next<R> next) {
    var logger = logger(command);
    return correlationId.wrap(
        () -> {
          logger.info(">>> {}", command);
          var response = next.invoke();
          logger.info("<<< {}", response);
          return response;
        });
  }

  private <R, C extends Command<R>> Logger logger(C command) {
    return LoggerFactory.getLogger(command.getClass());
  }
}