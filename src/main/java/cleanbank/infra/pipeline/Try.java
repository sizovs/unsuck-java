package cleanbank.infra.pipeline;

import cleanbank.infra.spring.annotations.PrototypeScoped;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

public class Try<C extends Command<R>, R> implements Command<R> {

  public final C origin;

  public int times = 3;

  public Try(C origin) {
    this.origin = origin;
  }

  public Try<C, R> times(int times) {
    this.times = times;
    return this;
  }

  public R now() {
    return origin.now();
  }

  @PrototypeScoped
  static class Reaction<C extends Command<R>, R> implements Command.Reaction<Try<C, R>, R> {

    @Override
    public R react(Try<C, R> command) {
      var retryPolicy = new SimpleRetryPolicy(command.times);

      var template = new RetryTemplate();
      template.setRetryPolicy(retryPolicy);

      return template.execute(context -> command.now());
    }
  }

}
