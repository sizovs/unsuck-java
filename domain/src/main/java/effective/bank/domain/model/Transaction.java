package effective.bank.domain.model;

import de.huxhorn.sulky.ulid.ULID;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import effective.bank.utils.TimeMachine;
import org.threeten.extra.LocalDateRange;

@Embeddable
public class Transaction {
    private static final ULID ULID_GENERATOR = new ULID();

    private String uid;
    private Amount amount;
    private LocalDateTime bookingTime;

    @Enumerated(EnumType.STRING)
    private Type type;

    private Transaction(Type type, Amount amount, LocalDateTime bookingTime) {
        this.uid = ULID_GENERATOR.nextULID();
        this.type = type;
        this.amount = amount;
        this.bookingTime = bookingTime;
    }

    private Transaction() {
    }

    public String uid() {
        return uid;
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
}