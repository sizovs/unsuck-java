package cleanbank.domains.accounts;

import cleanbank.infra.modeling.DomainEvent;

import java.math.BigDecimal;

record WithdrawalHappened(Iban iban, BigDecimal amount) implements DomainEvent {
}
