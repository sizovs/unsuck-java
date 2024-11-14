package cleanbank.domains.accounts;

import cleanbank.infra.modeling.DomainEntity;
import cleanbank.infra.modeling.VisibleForHibernate;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import jakarta.persistence.*;
import one.util.streamex.StreamEx;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static cleanbank.infra.time.TimeMachine.today;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

@Entity
public class BankAccount extends DomainEntity<BankAccount> {

  enum Status {
    NEW,
    OPEN,
    CLOSED
  }

  @Embedded
  private Iban iban;

  @Enumerated(EnumType.STRING)
  private Status status = Status.NEW;

  @Embedded
  private WithdrawalLimits withdrawalLimits;

  @Column
  private UUID clientId;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "BANK_ACCOUNT_IBAN")
  @OrderBy("ID ASC")
  private List<Transaction> transactions = new ArrayList<>();

  public BankAccount(Iban iban, UUID clientId, WithdrawalLimits withdrawalLimits) {
    this.withdrawalLimits = withdrawalLimits;
    this.clientId = clientId;
    this.iban = iban;
  }

  @VisibleForHibernate
  private BankAccount() {
  }

  public Iban iban() {
    return iban;
  }

  @VisibleForTesting
  List<Transaction> transactions() {
    return ImmutableList.copyOf(transactions);
  }

  public void open() {
    this.status = Status.OPEN;
    publish(new BankAccountOpened(iban, today()));
  }

  public void lift(WithdrawalLimits newLimits) {
    checkArgument(newLimits.areGreaterOrEqualTo(withdrawalLimits), "New limits should be greater or equal to the current limits");
    this.withdrawalLimits = newLimits;
  }

  public Transaction withdraw(BigDecimal amount) {
    checkState(isOpen(), "Account is not open.");

    var tx = Transaction.withdrawalOf(amount);
    transactions.add(tx);

    checkState(balance().signum() >= 0, "Insufficient funds.");
    checkState(isWithinMonthlyLimit(), "Monthly withdrawal limit reached.");
    checkState(isWithinDailyLimit(), "Daily withdrawal limit reached.");

    publish(new WithdrawalHappened(iban, tx));

    return tx;
  }

  private boolean isWithinMonthlyLimit() {
    var thisMonth = today().getMonth();
    var monthlyLimit = withdrawalLimits.monthlyLimit();
    var monthlyLimitReached = withdrawn(thisMonth).compareTo(monthlyLimit) > 0;
    return !monthlyLimitReached;
  }

  private BigDecimal withdrawn(Month month) {
    return StreamEx.of(transactions)
      .filter(tx -> tx.isBookedIn(month))
      .filter(tx -> tx.isWithdrawal())
      .foldRight(BigDecimal.ZERO, Transaction::apply)
      .abs();
  }

  private boolean isWithinDailyLimit() {
    var dailyLimit = withdrawalLimits.dailyLimit();
    var dailyLimitReached = withdrawn(today()).compareTo(dailyLimit) > 0;
    return !dailyLimitReached;
  }

  private BigDecimal withdrawn(LocalDate withdrawalDay) {
    return StreamEx.of(transactions)
      .filter(tx -> tx.bookingDate().isEqual(withdrawalDay))
      .filter(tx -> tx.isWithdrawal())
      .foldRight(BigDecimal.ZERO, Transaction::apply)
      .abs();
  }

  public Transaction deposit(BigDecimal amount) {
    checkState(isOpen(), "Account is not open.");

    var tx = Transaction.depositOf(amount);
    transactions.add(tx);

    return tx;
  }

  public BankStatement statement(LocalDate fromInclusive, LocalDate toInclusive) {
    return new BankStatement(fromInclusive, toInclusive, transactions);
  }

  public BigDecimal balance() {
    return StreamEx.of(transactions).foldRight(BigDecimal.ZERO, Transaction::apply);
  }

  @VisibleForTesting
  WithdrawalLimits withdrawalLimits() {
    return withdrawalLimits;
  }

  public void close(UnsatisfiedObligations unsatisfiedObligations) {
    checkState(!unsatisfiedObligations.exist(clientId), "Unsatisfied obligations exist.");
    status = Status.CLOSED;
  }

  public boolean isOpen() {
    return status.equals(Status.OPEN);
  }

  public boolean isClosed() {
    return status.equals(Status.CLOSED);
  }

}
