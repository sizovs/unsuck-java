package effective.bank.domain.model;


import org.apache.commons.validator.routines.IBANValidator;

import static com.google.common.base.Preconditions.checkArgument;

public class Iban {

    private final String iban;

    Iban(String safeIban) {
        this.iban = safeIban;
    }

    public Iban(String unsafeIban, IbanUniqueness uniqueness) {
        var isValid = isValid(unsafeIban);
        checkArgument(isValid, "Iban %s doesn't conform to IBAN spec", unsafeIban);

        var isUnique = uniqueness.guaranteed(unsafeIban);
        checkArgument(isUnique, "Iban %s has been taken", unsafeIban);
        this.iban = unsafeIban;
    }

    public static boolean isValid(String unsafeIban) {
        return IBANValidator.getInstance().isValid(unsafeIban);
    }

    @Override
    public int hashCode() {
        return iban.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Iban that) {
            return this.iban.equals(that.iban);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return iban;
    }
}
