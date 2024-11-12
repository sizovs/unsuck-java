package cleanbank.domains.accounts;

import cleanbank.infra.modeling.VisibleForHibernate;
import cleanbank.infra.time.TimeMachine;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

@Entity
public class Transaction {

  @Id
  @GeneratedValue
  private Long id;

  private BigDecimal amount;
  private LocalDateTime bookingTime;

  @Enumerated(EnumType.STRING)
  private Type type;

  private Transaction(Type type, BigDecimal amount, LocalDateTime bookingTime) {
    this.type = type;
    this.amount = amount;
    this.bookingTime = bookingTime;
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
    return type == Type.WITHDRAWAL;
  }

  public BigDecimal deposited() {
    return isDeposit() ? amount : BigDecimal.ZERO;
  }

  boolean isDeposit() {
    return type == Type.DEPOSIT;
  }

  LocalDateTime bookingTime() {
    return bookingTime;
  }

  LocalDate bookingDate() {
    return bookingTime.toLocalDate();
  }

  boolean isBookedIn(Month month) {
    return bookingDate().getMonth().equals(month);
  }

  boolean isBookedBefore(LocalDate dateExclusive) {
    return bookingDate().isBefore(dateExclusive);
  }

  boolean isBookedDuring(LocalDate fromInclusive, LocalDate toInclusive) {
    return bookingDate().isEqual(fromInclusive) || bookingDate().isEqual(toInclusive)
      || (bookingDate().isAfter(fromInclusive) && bookingDate().isBefore(toInclusive));
  }

  static Transaction withdrawalOf(BigDecimal amount) {
    return new Transaction(Type.WITHDRAWAL, amount, TimeMachine.currentTime());
  }

  static Transaction depositOf(BigDecimal amount) {
    return new Transaction(Type.DEPOSIT, amount, TimeMachine.currentTime());
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
