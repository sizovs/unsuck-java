package effective.bank.domain.model;

import java.time.LocalDate;

record BankAccountOpened(Iban iban, LocalDate date) implements DomainEvent {
}