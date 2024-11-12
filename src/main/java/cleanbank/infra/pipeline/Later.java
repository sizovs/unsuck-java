package cleanbank.infra.pipeline;

import org.springframework.stereotype.Component;

import java.util.Optional;

public interface Later {

  ThreadLocal<Later> INSTANCE = InheritableThreadLocal.withInitial(() -> Spring.LATER.orElseThrow());

  @Component
  class Spring {
    private static Optional<Later> LATER = Optional.empty();

    Spring(Later later) {
      LATER = Optional.of(later);
    }
  }

  <C extends Command<?>> void enqueue(C command);
}
