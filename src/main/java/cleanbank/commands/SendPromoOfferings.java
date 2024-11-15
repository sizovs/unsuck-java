package cleanbank.commands;

import cleanbank.infra.locks.Locking;
import cleanbank.infra.pipeline.Command;
import cleanbank.infra.spring.annotations.PrototypeScoped;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

public class SendPromoOfferings implements Command<Command.Void> {

  @PrototypeScoped
  static class Reaction implements Command.Reaction<SendPromoOfferings, Void> {

    @Override
    public Void react(SendPromoOfferings cmd) {
      return null;
    }
  }

  @Component
  static class EveryMidnight {
    private final Locking locking;

    EveryMidnight(Locking locking) {
      this.locking = locking;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void run() {
      locking.runExclusive("SEND_PROMOS", () -> {
        new SendPromoOfferings().now();
      });
    }
  }

}
