package cleanbank.domains.accounts;

import cleanbank.infra.modeling.DomainEvent;

record WithdrawalHappened(Iban iban, Transaction tx) implements DomainEvent {
}
