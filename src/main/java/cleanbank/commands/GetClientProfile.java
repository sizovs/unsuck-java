package cleanbank.commands;

import cleanbank.infra.pipeline.Command;
import cleanbank.infra.pipeline.ReadOnly;
import cleanbank.infra.spring.annotations.PrototypeScoped;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

// Queries, unlike commands, can access database directly, bypassing heavy Spring Data/JPA/Hibernate machinery.
public record GetClientProfile(UUID clientId) implements Command<GetClientProfile.Profile>, ReadOnly {

  public record Profile(String email, String firstName, String lastName) {
  }

  @PrototypeScoped
  static class Reaction implements Command.Reaction<GetClientProfile, Profile> {

    private final JdbcTemplate db;

    Reaction(JdbcTemplate db) {
      this.db = db;
    }

    @Override
    public Profile react(GetClientProfile cmd) {
      var sql = "SELECT email, first_name, last_name FROM Client where id = ?";
      return db.queryForObject(sql, new DataClassRowMapper<>(Profile.class), cmd.clientId());
    }
  }

}
