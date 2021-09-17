package effective.bank.application.infra.pipeline.middlewares.logging;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

@Component
class CorrelationId {

  private static final String MDC_KEY = "ccid";

  private final AtomicLong counter = new AtomicLong();

  <T> T wrap(Supplier<T> action) {

    var closeable = MDC.putCloseable(MDC_KEY, next());
    try (closeable) {
      return action.get();
    }
  }

  private String next() {
    return String.valueOf(counter.incrementAndGet() % 1000);
  }
}