package cleanbank.domains.accounts;

import cleanbank.infra.modeling.DomainEntity;
import cleanbank.infra.modeling.VisibleForHibernate;
import cleanbank.infra.time.TimeMachine;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import jakarta.persistence.*;
import one.util.streamex.StreamEx;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

@Entity
public class BankAccount extends DomainEntity<BankAccount> {

  enum Status {
    NEW,
    OPEN,
    SUSPENDED,
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
    publish(new BankAccountOpened(iban, TimeMachine.today()));
  }

  public void suspend() {
    checkState(!this.status.equals(Status.SUSPENDED), "Bank account is already suspended");
    this.status = Status.SUSPENDED;
  }

  public void lift(WithdrawalLimits newLimits) {
    checkArgument(newLimits.areGreaterOrEqualTo(withdrawalLimits), "New limits should be greater or equal to the current limits");
    this.withdrawalLimits = newLimits;
  }

  public Transaction withdraw(BigDecimal amount) {
    new EnforceOpen();

    var tx = Transaction.withdrawalOf(amount);
    transactions.add(tx);

    new EnforcePositiveBalance();
    new EnforceMonthlyWithdrawalLimit();
    new EnforceDailyWithdrawalLimit();

    publish(new WithdrawalHappened(iban, tx));

    return tx;
  }

  public Transaction deposit(BigDecimal amount) {
    new EnforceOpen();

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
    checkState(
      !unsatisfiedObligations.exist(clientId),
      "Bank account cannot be closed while client has unsatisfied obligations");
    status = Status.CLOSED;
  }

  public boolean isOpen() {
    return status.equals(Status.OPEN);
  }

  public boolean isClosed() {
    return status.equals(Status.CLOSED);
  }

  private class EnforceOpen {
    private EnforceOpen() {
      checkState(isOpen(), "Account is not open.");
    }
  }

  private class EnforcePositiveBalance {
    private EnforcePositiveBalance() {
      checkState(balance().signum() >= 0, "Not enough funds available on your account.");
    }
  }

  private class EnforceDailyWithdrawalLimit {
    private EnforceDailyWithdrawalLimit() {
      var dailyLimit = withdrawalLimits.dailyLimit();
      var dailyLimitReached = withdrawn(TimeMachine.today()).compareTo(dailyLimit) > 0;
      Preconditions.checkState(!dailyLimitReached, "Daily withdrawal limit (%s) reached.", dailyLimit);
    }

    private BigDecimal withdrawn(LocalDate withdrawalDay) {
      return StreamEx.of(transactions)
        .filter(tx -> tx.bookingDate().isEqual(withdrawalDay))
        .filter(tx -> tx.isWithdrawal())
        .foldRight(BigDecimal.ZERO, Transaction::apply)
        .abs();
    }
  }

  private class EnforceMonthlyWithdrawalLimit {
    private EnforceMonthlyWithdrawalLimit() {
      var thisMonth = TimeMachine.today().getMonth();
      var monthlyLimit = withdrawalLimits.monthlyLimit();
      var monthlyLimitReached = withdrawn(thisMonth).compareTo(monthlyLimit) > 0;
      Preconditions.checkState(!monthlyLimitReached, "Monthly withdrawal limit (%s) reached.", monthlyLimit);
    }

    private BigDecimal withdrawn(Month month) {
      return StreamEx.of(transactions)
        .filter(tx -> tx.isBookedIn(month))
        .filter(tx -> tx.isWithdrawal())
        .foldRight(BigDecimal.ZERO, Transaction::apply)
        .abs();
    }
  }

}
