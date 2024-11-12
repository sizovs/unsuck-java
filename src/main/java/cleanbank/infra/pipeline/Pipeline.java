package cleanbank.infra.pipeline;

import cleanbank.infra.pipeline.Command.Reaction;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
class Pipeline implements Now {

  private final Now pipeline;

  @SuppressWarnings("rawtypes")
  Pipeline(PlatformTransactionManager txManager, ObjectProvider<Reaction> reactions, ObjectProvider<RateLimiter<Command<?>>> rateLimiters) {
    this.pipeline =
      new Correlable(
        new Loggable(
          new RateLimiting(rateLimiters,
            new Transactional(txManager,
              new Reacting(reactions)
            )
          )
        )
      );
  }

  @Override
  public <C extends Command<R>, R> R execute(C command) {
    return pipeline.execute(command);
  }
}


