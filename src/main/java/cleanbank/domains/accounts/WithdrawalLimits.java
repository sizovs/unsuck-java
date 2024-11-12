package cleanbank.domains.accounts;

import jakarta.persistence.Embeddable;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkState;

@Embeddable
@ConfigurationProperties("withdrawal-limits")
public record WithdrawalLimits(BigDecimal dailyLimit, BigDecimal monthlyLimit) {

  public WithdrawalLimits {
    var withinLimits = monthlyLimit.compareTo(dailyLimit) >= 0;
    checkState(
      withinLimits,
      "Monthly limit (%s) must be higher or equal to daily limit (%s)",
      monthlyLimit,
      dailyLimit);

  }

  boolean areGreaterOrEqualTo(WithdrawalLimits otherLimits) {
    return dailyLimit.compareTo(otherLimits.dailyLimit) >= 0 && monthlyLimit.compareTo(otherLimits.monthlyLimit) >= 0;
  }


}
