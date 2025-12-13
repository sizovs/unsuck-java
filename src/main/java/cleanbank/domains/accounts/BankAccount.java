package cleanbank.domains.accounts;

import cleanbank.infra.modeling.DomainEntity;
import cleanbank.infra.modeling.VisibleForHibernate;
import cleanbank.infra.time.TimeMachine;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import jakarta.persistence.*;
import one.util.streamex.StreamEx;
import org.threeten.extra.LocalDateRange;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static cleanbank.domains.accounts.BankAccount.Transaction.Type.DEPOSIT;
import static cleanbank.domains.accounts.BankAccount.Transaction.Type.WITHDRAWAL;
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

  private UUID clientId;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "BANK_ACCOUNT_IBAN", nullable = false, updatable = false)
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

    var tx = new Transaction(WITHDRAWAL, amount);
    transactions.add(tx);

    checkState(balance().signum() >= 0, "Insufficient funds.");
    checkState(isWithinMonthlyLimit(), "Monthly withdrawal limit reached.");
    checkState(isWithinDailyLimit(), "Daily withdrawal limit reached.");

    publish(new WithdrawalHappened(iban, tx.amount));

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

    var tx = new Transaction(DEPOSIT, amount);
    transactions.add(tx);

    return tx;
  }

  public BankStatement statement(LocalDateRange period) {
    return new BankStatement(period, transactions);
  }

  public BigDecimal balance() {
    return StreamEx.of(transactions).foldRight(BigDecimal.ZERO, Transaction::apply);
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

  @VisibleForTesting
  WithdrawalLimits withdrawalLimits() {
    return withdrawalLimits;
  }

  @Entity
  public static class Transaction {

    @Id
    @GeneratedValue
    private Long id;

    private BigDecimal amount;
    private LocalDateTime bookingTime;

    @Enumerated(EnumType.STRING)
    private Type type;

    private Transaction(Type type, BigDecimal amount) {
      this.type = type;
      this.amount = amount;
      this.bookingTime = TimeMachine.currentTime();
    }

    @VisibleForHibernate
    private Transaction() {
    }

    BigDecimal apply(BigDecimal balance) {
      return type.apply(amount, balance);
    }

    public BigDecimal withdrawn() {
      return isWithdrawal() ? amount : BigDecimal.ZERO;
    }

    boolean isWithdrawal() {
      return type == WITHDRAWAL;
    }

    public BigDecimal deposited() {
      return isDeposit() ? amount : BigDecimal.ZERO;
    }

    boolean isDeposit() {
      return type == DEPOSIT;
    }

    boolean isBookedIn(Month month) {
      return bookingDate().getMonth().equals(month);
    }

    boolean isBookedBefore(LocalDate dateExclusive) {
      return bookingDate().isBefore(dateExclusive);
    }

    LocalDate bookingDate() {
      return bookingTime.toLocalDate();
    }

    LocalDateTime bookingTime() {
      return bookingTime;
    }

    enum Type {
      DEPOSIT {
        @Override
        BigDecimal apply(BigDecimal amount, BigDecimal balance) {
          return balance.add(amount);
        }
      },
      WITHDRAWAL {
        @Override
        BigDecimal apply(BigDecimal amount, BigDecimal balance) {
          return balance.subtract(amount);
        }
      };

      abstract BigDecimal apply(BigDecimal amount, BigDecimal balance);
    }

  }
}
