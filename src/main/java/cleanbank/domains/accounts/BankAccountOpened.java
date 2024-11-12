package cleanbank.domains.accounts;

import cleanbank.infra.modeling.DomainEvent;

import java.time.LocalDate;

public record BankAccountOpened(Iban iban, LocalDate date) implements DomainEvent {
}
