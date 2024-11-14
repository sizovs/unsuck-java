package cleanbank.infra.pipeline;

import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicLong;

class Correlable implements Now {

  private final AtomicLong counter = new AtomicLong();

  private final Now origin;

  Correlable(Now origin) {
    this.origin = origin;
  }

  @Override
  public <C extends Command<R>, R> R execute(C command) {
    var withCid = MDC.putCloseable("cid", nextCid());
    try (withCid) {
      return origin.execute(command);
    }
  }

  private String nextCid() {
    return String.valueOf(counter.incrementAndGet() % 1000);
  }
}
