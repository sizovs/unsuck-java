package cleanbank.commands;

import cleanbank.domains.crm.Clients;
import cleanbank.infra.pipeline.Command;
import cleanbank.infra.spring.annotations.PrototypeScoped;

import java.util.UUID;


public record GetClientProfile(UUID clientID) implements Command<GetClientProfile.Profile> {

  public record Profile(String email, String firstName, String lastName) {
  }

  @PrototypeScoped
  static class Reaction implements Command.Reaction<GetClientProfile, Profile> {

    private final Clients clients;

    Reaction(Clients clients) {
      this.clients = clients;
    }

    @Override
    public Profile react(GetClientProfile cmd) {
      var client = clients.findById(cmd.clientID());
      return new Profile(client.email(), client.firstName(), client.lastName());
    }
  }

}
