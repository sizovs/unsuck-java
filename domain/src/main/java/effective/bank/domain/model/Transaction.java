package effective.bank.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import javax.persistence.*;

import effective.bank.utils.TimeMachine;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.threeten.extra.LocalDateRange;

@Entity
public class Transaction {

    @Id
    @GeneratedValue

    private Long id;
    private Amount amount;
    private LocalDateTime bookingTime;

    @Enumerated(EnumType.STRING)
    private Type type;

    private Transaction(Type type, Amount amount, LocalDateTime bookingTime) {
        this.type = type;
        this.amount = amount;
        this.bookingTime = bookingTime;
    }

    private Transaction() {
    }

    Amount apply(Amount balance) {
        return type.apply(amount, balance);
    }

    public Amount withdrawn() {
        return isWithdrawal() ? amount : Amount.ZERO;
    }

    boolean isWithdrawal() {
        return type == Type.WITHDRAW;
    }

    public Amount deposited() {
        return isDeposit() ? amount : Amount.ZERO;
    }

    boolean isDeposit() {
        return type == Type.DEPOSIT;
    }

    LocalDateTime bookingTime() {
        return bookingTime;
    }

    boolean isBookedOn(LocalDate date) {
        return bookingTime.toLocalDate().isEqual(date);
    }

    boolean isBookedBefore(LocalDate dateExclusive) {
        return LocalDateRange.ofUnboundedStart(dateExclusive).contains(bookingTime.toLocalDate());
    }

    boolean isBookedDuring(LocalDate fromInclusive, LocalDate toInclusive) {
        return LocalDateRange.ofClosed(fromInclusive, toInclusive).contains(bookingTime.toLocalDate());
    }

    boolean isBookedIn(Month month) {
        return bookingTime.toLocalDate().getMonth().equals(month);
    }

    static Transaction withdrawalOf(Amount amount) {
        return new Transaction(Type.WITHDRAW, amount, TimeMachine.currentTime());
    }

    static Transaction depositOf(Amount amount) {
        return new Transaction(Type.DEPOSIT, amount, TimeMachine.currentTime());
    }

    enum Type {
        DEPOSIT {
            @Override
            Amount apply(Amount amount, Amount balance) {
                return balance.add(amount);
            }
        },
        WITHDRAW {
            @Override
            Amount apply(Amount amount, Amount balance) {
                return balance.subtract(amount);
            }
        };

        abstract Amount apply(Amount amount, Amount balance);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Transaction that) {
            return this.id.equals(that.id);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE).append(amount).build();
    }
}