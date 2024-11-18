package cleanbank.commands;

import cleanbank.domains.crm.Client;
import cleanbank.domains.crm.Clients;
import cleanbank.infra.pipeline.Command;
import cleanbank.infra.spring.annotations.PrototypeScoped;
import cleanbank.infra.validation.Rules;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

public record RegisterAsClient(
  String personalId,
  String firstName,
  String lastName,
  String email) implements Command<UUID> {

  @PrototypeScoped
  static class Reaction implements Command.Reaction<RegisterAsClient, UUID> {

    private final Clients clients;

    public Reaction(Clients clients) {
      this.clients = clients;
    }

    @Override
    public UUID react(RegisterAsClient cmd) {
      new Rules<RegisterAsClient>()
        .define(cmd::personalId, StringUtils::isNotEmpty, "personalId must not be empty")
        .define(cmd::firstName, StringUtils::isNotEmpty, "firstName must not be empty")
        .define(cmd::lastName, StringUtils::isNotEmpty, "lastName must not be empty")
        .define(cmd::email, StringUtils::isNotEmpty, "email must not be empty")
        .enforce(cmd);

      var client = new Client(cmd.personalId(), cmd.firstName(), cmd.lastName(), cmd.email());
      clients.add(client);

      return client.id();
    }
  }
}

