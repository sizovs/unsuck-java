package cleanbank.infra.pipeline;

import org.slf4j.LoggerFactory;

class Loggable implements Now {

  private final Now origin;

  Loggable(Now origin) {
    this.origin = origin;
  }

  @Override
  public <C extends Command<R>, R> R execute(C command) {
    var logger = LoggerFactory.getLogger(command.getClass());
    logger.info(">>> {}", command);
    var response = origin.execute(command);
    logger.info("<<< {}", response);
    return response;
  }

}
