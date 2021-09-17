package effective.bank.application.infra.scheduling;

import an.awesome.pipelinr.Command;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;

import static com.machinezoo.noexception.Exceptions.wrap;

public class CommandJpaConverter implements AttributeConverter<Command, String> {

    private final ObjectMapper mapper = new CommandJsonMapper();

    @Override
    public String convertToDatabaseColumn(Command cmd) {
        return wrap().get(() -> mapper.writeValueAsString(cmd));
    }

    @Override
    public Command convertToEntityAttribute(String serializedCmd) {
        return wrap().get(() -> mapper.readValue(serializedCmd, Command.class));
    }

}