package cleanbank.infra.pipeline;

import com.google.common.reflect.TypeToken;
import io.github.bucket4j.Bandwidth;

public interface RateLimiter<C extends Command<?>> {

  Bandwidth bandwidth();

  default boolean matches(C command) {
    var commandTypeToken = new TypeToken<C>(getClass()) {
    };
    return commandTypeToken.isSupertypeOf(command.getClass());
  }


}
