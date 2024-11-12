package cleanbank.infra.pipeline;

import org.springframework.stereotype.Component;

import java.util.Optional;

public interface Now {

  ThreadLocal<Now> INSTANCE = InheritableThreadLocal.withInitial(() -> Spring.NOW.orElseThrow());

  @Component
  class Spring {
    private static Optional<Now> NOW = Optional.empty();

    Spring(Now now) {
      NOW = Optional.of(now);
    }
  }

  <C extends Command<R>, R> R execute(C command);
}
