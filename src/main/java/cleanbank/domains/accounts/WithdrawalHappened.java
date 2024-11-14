package cleanbank.domains.accounts;

import cleanbank.infra.modeling.DomainEvent;

record WithdrawalHappened(Iban iban, BankAccount.Transaction tx) implements DomainEvent {
}
