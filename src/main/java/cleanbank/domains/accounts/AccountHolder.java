package cleanbank.domains.accounts;

import jakarta.persistence.Embeddable;

@Embeddable
public record AccountHolder(String firstName, String lastName, String email) {

  public String name() {
    return firstName + " " + lastName;
  }
}
