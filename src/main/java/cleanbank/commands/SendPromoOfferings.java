package cleanbank.commands;

import cleanbank.infra.pipeline.ClusterOnce;
import cleanbank.infra.pipeline.Command;
import cleanbank.infra.spring.annotations.PrototypeScoped;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

public class SendPromoOfferings implements Command<Command.Void> {

  @PrototypeScoped
  static class Reaction implements Command.Reaction<SendPromoOfferings, Void> {

    @Override
    public Void react(SendPromoOfferings cmd) {
      System.out.println("Sending a bunch of spam");
      return new Void();
    }
  }

  @Component
  static class EveryMidnight {

    @Scheduled(cron = "0 0 0 * * ?")
    public void run() {
      new ClusterOnce<>(
        new SendPromoOfferings()
      ).now();
    }
  }

}
