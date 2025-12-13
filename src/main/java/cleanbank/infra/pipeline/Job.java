package cleanbank.infra.pipeline;

import cleanbank.infra.modeling.VisibleForHibernate;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import tools.jackson.databind.json.JsonMapper;
import static com.machinezoo.noexception.Exceptions.wrap;

@Entity
class Job {

  @Id
  @GeneratedValue
  private Long id;

  private Command<?> command;

  Job(Command<?> command) {
    this.command = command;
  }

  @VisibleForHibernate
  private Job() {
  }

  void execute() {
    command.now();
  }

  @Converter(autoApply = true)
  static class CommandJsonMapper implements AttributeConverter<Command<?>, String> {

    @JsonTypeInfo(
      use = JsonTypeInfo.Id.CLASS,
      include = JsonTypeInfo.As.PROPERTY,
      property = "@class"
    )
    public abstract static class CommandMixIn {}

    JsonMapper mapper = JsonMapper.builder()
      .addMixIn(Command.class, CommandMixIn.class)
      .build();

    @Override
    public String convertToDatabaseColumn(Command<?> cmd) {
      return wrap().get(() -> mapper.writeValueAsString(cmd));
    }

    @Override
    public Command<?> convertToEntityAttribute(String cmd) {
      return wrap().get(() -> mapper.readValue(cmd, Command.class));
    }
  }

}
