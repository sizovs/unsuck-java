package effective.bank.application.infra.scheduling;

import an.awesome.pipelinr.Command;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;

import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.EVERYTHING;

class CommandJsonMapper extends JsonMapper {

    public CommandJsonMapper() {
        this.activateDefaultTyping(BasicPolymorphicTypeValidator.builder().allowIfBaseType(Command.class).build(), EVERYTHING);
        this.findAndRegisterModules();
    }

    @Override
    protected TypeResolverBuilder<?> _constructDefaultTypeResolverBuilder(DefaultTyping applicability, PolymorphicTypeValidator ptv) {
        // This guy limits JsonMapper to include default types only for classes that are subtypes of Command.
        // We need this because Jackson (tested in 2.12.4), includes type info for standard library classes such as LocalDate etc.
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
}
