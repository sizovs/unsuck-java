package effective.bank.domain.model;

public record WithdrawalHappened(Iban iban, Transaction tx) implements DomainEvent {
}