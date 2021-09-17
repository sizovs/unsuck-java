package effective.bank.domain.model;

import effective.bank.utils.FunctionMemoizer;
import org.springframework.stereotype.Component;

public interface IbanUniqueness {

    IbanUniqueness GUARANTEED = iban -> true;

    default IbanUniqueness memoized() {
        return FunctionMemoizer.memoize(this::guaranteed)::apply;
    }

    boolean guaranteed(String iban);

    @Component
    record AcrossBankAccounts(BankAccountRepository repository) implements IbanUniqueness {
        @Override
        public boolean guaranteed(String iban) {
            System.out.println("Checking uniqueness of iban " + iban);
            return !repository.existsById(iban);
        }
    }

}
