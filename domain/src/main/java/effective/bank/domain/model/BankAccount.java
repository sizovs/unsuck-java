package effective.bank.domain.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import effective.bank.utils.TimeMachine;
import one.util.streamex.StreamEx;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

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

    @Id
    private String iban;

    @Enumerated(EnumType.STRING)
    private Status status = Status.NEW;

    @Embedded
    private WithdrawalLimits withdrawalLimits;

    @Embedded
    private AccountHolder holder;

    @Version
    private long version;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "BANK_ACCOUNT_IBAN")
    @OrderBy("ID ASC")
    private List<Transaction> transactions = new ArrayList<>();

    public BankAccount(Iban iban, AccountHolder holder, WithdrawalLimits withdrawalLimits) {
        this.withdrawalLimits = withdrawalLimits;
        this.holder = holder;
        this.iban = iban.toString();
    }

    BankAccount() {
    }

    public AccountHolder holder() {
        return holder;
    }

    public Iban iban() {
        return new Iban(iban);
    }

    @VisibleForTesting
    List<Transaction> transactions() {
        return ImmutableList.copyOf(transactions);
    }

    public void open() {
        this.status = Status.OPEN;
        publish(new BankAccountOpened(iban(), TimeMachine.today()));
    }

    public void suspend() {
        checkState(!this.status.equals(Status.SUSPENDED), "Bank account is already suspended");
        this.status = Status.SUSPENDED;
    }

    public void lift(WithdrawalLimits newLimits) {
        checkArgument(newLimits.areGreaterOrEqualTo(withdrawalLimits), "New limits should be greater or equal to the current limits");
        this.withdrawalLimits = newLimits;
    }

    public Transaction withdraw(Amount amount) {
        new EnforceOpen();

        var tx = Transaction.withdrawalOf(amount);
        transactions.add(tx);

        new EnforcePositiveBalance();
        new EnforceMonthlyWithdrawalLimit();
        new EnforceDailyWithdrawalLimit();

        publish(new WithdrawalHappened(iban(), tx));

        return tx;
    }

    public Transaction deposit(Amount amount) {
        new EnforceOpen();

        var tx = Transaction.depositOf(amount);
        transactions.add(tx);

        return tx;
    }

    public BankStatement statement(LocalDate fromInclusive, LocalDate toInclusive) {
        return new BankStatement(fromInclusive, toInclusive, transactions);
    }

    public Amount balance() {
        return StreamEx.of(transactions).foldRight(Amount.ZERO, Transaction::apply);
    }

    WithdrawalLimits withdrawalLimits() {
        return withdrawalLimits;
    }

    public void close(UnsatisfiedObligations unsatisfiedObligations) {
        checkState(
                !unsatisfiedObligations.exist(),
                "Bank account cannot be closed because a holder has unsatisfied obligations");
        status = Status.CLOSED;
    }

    public boolean isOpen() {
        return status.equals(Status.OPEN);
    }

    public boolean isClosed() {
        return status.equals(Status.CLOSED);
    }

    public boolean isSuspended() {
        return status.equals(Status.SUSPENDED);
    }

    private class EnforceOpen {
        private EnforceOpen() {
            checkState(isOpen(), "Account is not open.");
        }
    }

    private class EnforcePositiveBalance {

        private EnforcePositiveBalance() {
            checkState(balance().isPositive(), "Not enough funds available on your account.");
        }
    }

    private class EnforceDailyWithdrawalLimit {

        private EnforceDailyWithdrawalLimit() {
            var dailyLimit = withdrawalLimits.dailyLimit;
            var dailyLimitReached = withdrawn(TimeMachine.today()).isGreaterThan(dailyLimit);
            checkState(!dailyLimitReached, "Daily withdrawal limit (%s) reached.", dailyLimit);
        }

        private Amount withdrawn(LocalDate withdrawalDay) {
            return StreamEx.of(transactions)
                    .filter(tx -> tx.isBookedOn(withdrawalDay))
                    .filter(tx -> tx.isWithdrawal())
                    .foldRight(Amount.ZERO, Transaction::apply)
                    .abs();
        }
    }

    private class EnforceMonthlyWithdrawalLimit {

        private EnforceMonthlyWithdrawalLimit() {
            var thisMonth = TimeMachine.today().getMonth();
            var monthlyLimit = withdrawalLimits.monthlyLimit;
            var monthlyLimitReached = withdrawn(thisMonth).isGreaterThan(monthlyLimit);
            checkState(!monthlyLimitReached, "Monthly withdrawal limit (%s) reached.", monthlyLimit);
        }

        private Amount withdrawn(Month month) {
            return StreamEx.of(transactions)
                    .filter(tx -> tx.isBookedIn(month))
                    .filter(tx -> tx.isWithdrawal())
                    .foldRight(Amount.ZERO, Transaction::apply)
                    .abs();
        }
    }


}