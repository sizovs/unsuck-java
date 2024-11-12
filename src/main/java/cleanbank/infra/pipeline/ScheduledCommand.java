package cleanbank.infra.pipeline;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import jakarta.persistence.*;

import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL_AND_ENUMS;
import static com.machinezoo.noexception.Exceptions.wrap;

@Entity
class ScheduledCommand {

  @Id
  @GeneratedValue
  private Long id;

  private Command<?> command;

  ScheduledCommand(Command<?> command) {
    this.command = command;
  }

  private ScheduledCommand() {
  }

  void now() {
    command.now();
  }

  @Converter(autoApply = true)
  static class CommandJsonMapper extends JsonMapper implements AttributeConverter<Command<?>, String> {

    public CommandJsonMapper() {
      this.activateDefaultTyping(BasicPolymorphicTypeValidator.builder().allowIfBaseType(Command.class).build(), NON_FINAL_AND_ENUMS);
      this.findAndRegisterModules();
    }

    @Override
    protected TypeResolverBuilder<?> _constructDefaultTypeResolverBuilder(DefaultTyping applicability, PolymorphicTypeValidator ptv) {
      // Restrict JsonMapper to include types only for classes that are subtypes of Command.
      // Everything else Jackson can de-serialize (LocalDate, etc.)
      class CommandTypeResolverBuilder extends DefaultTypeResolverBuilder {
        public CommandTypeResolverBuilder(DefaultTyping defaultTyping, PolymorphicTypeValidator ptv) {
          super(defaultTyping, ptv);
        }

        @Override
        public boolean useForType(JavaType t) {
          return t.isTypeOrSubTypeOf(Command.class);
        }
      }
      return new CommandTypeResolverBuilder(applicability, ptv);
    }

    @Override
    public String convertToDatabaseColumn(Command cmd) {
      return wrap().get(() -> writeValueAsString(cmd));
    }

    @Override
    public Command<?> convertToEntityAttribute(String cmd) {
      return wrap().get(() -> readValue(cmd, Command.class));
    }
  }

}
