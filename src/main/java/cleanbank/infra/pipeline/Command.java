package cleanbank.infra.pipeline;

import com.google.common.reflect.TypeToken;

public interface Command<R> {

  default R now() {
    var now = Now.INSTANCE.get();
    return now.execute(this);
  }

  default void later() {
    var later = Later.INSTANCE.get();
    later.enqueue(this);
  }

  interface Reaction<C extends Command<R>, R> {

    R react(C cmd);

    default boolean matches(C command) {
      var commandTypeToken = new TypeToken<C>(getClass()) {
      };
      return commandTypeToken.getRawType().equals(command.getClass()) ||
        commandTypeToken.isSupertypeOf(command.getClass());
    }
  }

  class Void {
  }

}
