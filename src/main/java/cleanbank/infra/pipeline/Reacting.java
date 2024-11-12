package cleanbank.infra.pipeline;

import org.springframework.beans.factory.ObjectProvider;

@SuppressWarnings({"rawtypes", "unchecked"})
class Reacting implements Now {

  private final ObjectProvider<Command.Reaction> reactions;

  Reacting(ObjectProvider<Command.Reaction> reactions) {
    this.reactions = reactions;
  }

  @Override
  public <C extends Command<R>, R> R execute(C command) {
    var reaction = reactions
      .stream()
      .filter(it -> it.matches(command))
      .findFirst()
      .orElseThrow(() -> new NoReactionFoundException(command));

    return (R) reaction.react(command);
  }

  static class NoReactionFoundException extends RuntimeException {
    NoReactionFoundException(Command command) {
      super("Cannot find reaction for command " + command.getClass().getSimpleName());
    }
  }
}
