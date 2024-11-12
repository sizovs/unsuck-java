package cleanbank.infra

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender

class Logs {

  @Delegate
  private final List<ILoggingEvent> logEvents

  private final Logger logger

  private final ListAppender<ILoggingEvent> appender

  Logs(Logger logger) {
    this.appender = new ListAppender<ILoggingEvent>()
    this.logEvents = appender.list
    this.logger = logger
  }

  def listen() {
    appender.start()
    logger.addAppender(appender)
  }

  def forget() {
    appender.stop()
    logger.detachAppender(appender)
  }

}
