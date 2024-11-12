package cleanbank.domains.accounts;

import cleanbank.infra.modeling.Data;
import cleanbank.infra.modeling.VisibleForHibernate;
import jakarta.persistence.Embeddable;
import org.apache.commons.validator.routines.IBANValidator;

import static com.google.common.base.Preconditions.checkArgument;

@Embeddable
public class Iban extends Data {

  private String iban;

  @VisibleForHibernate
  private Iban() {
  }

  public Iban(String iban, Uniqueness uniqueness) {
    checkArgument(isValid(iban), "Iban %s doesn't conform to IBAN spec", iban);
    checkArgument(uniqueness.guaranteed(iban), "Iban %s is already taken", iban);
    this.iban = iban;
  }

  public static boolean isValid(String iban) {
    return IBANValidator.getInstance().isValid(iban);
  }

  @Override
  public String toString() {
    return iban;
  }

  public interface Uniqueness {

    Uniqueness GUARANTEED = iban -> true;

    boolean guaranteed(String iban);

  }
}
