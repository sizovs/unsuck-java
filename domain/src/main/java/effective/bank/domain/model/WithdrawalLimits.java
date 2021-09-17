package effective.bank.domain.model;

import org.springframework.core.env.Environment;

import static com.google.common.base.Preconditions.checkState;

import javax.persistence.Embeddable;

@Embeddable
public class WithdrawalLimits extends ValueObject {

    Amount dailyLimit;

    Amount monthlyLimit;

    public WithdrawalLimits(Environment env) {
        this(
                Amount.of(env.getProperty("banking.account-limits.daily")),
                Amount.of(env.getProperty("banking.account-limits.monthly")));
    }

    public WithdrawalLimits(Amount dailyLimit, Amount monthlyLimit) {
        var withinLimits = monthlyLimit.isGreaterThanOrEqualTo(dailyLimit);
        checkState(
                withinLimits,
                "Monthly limit (%s) must be higher or equal to daily limit (%s)",
                monthlyLimit,
                dailyLimit);

        this.dailyLimit = dailyLimit;
        this.monthlyLimit = monthlyLimit;
    }

    private WithdrawalLimits() {
    }
}