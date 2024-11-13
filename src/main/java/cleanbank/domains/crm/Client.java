package cleanbank.domains.crm;

import cleanbank.infra.modeling.DomainEntity;
import cleanbank.infra.modeling.VisibleForHibernate;
import jakarta.persistence.Entity;
import org.hibernate.annotations.NaturalId;

@Entity
public class Client extends DomainEntity<Client> {

  @NaturalId
  private String personalId;

  private String firstName;

  private String lastName;

  private String email;

  public Client(String personalId, String firstName, String lastName, String email) {
    this.personalId = personalId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
  }

  @VisibleForHibernate
  private Client() {
  }

  public String name() {
    return firstName + " " + lastName;
  }

  public String email() {
    return email;
  }

  public String firstName() {
    return firstName;
  }

  public String lastName() {
    return lastName;
  }
}
