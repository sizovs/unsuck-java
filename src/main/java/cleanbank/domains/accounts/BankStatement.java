package cleanbank.domains.accounts;

import com.fasterxml.jackson.databind.ObjectMapper;
import one.util.streamex.StreamEx;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

class BankStatement {

  private final Collection<Entry> entries = new ArrayList<>();
  private final BalanceSnapshot closingBalance;
  private final BalanceSnapshot startingBalance;

  BankStatement(LocalDate fromInclusive, LocalDate toInclusive, Collection<BankAccount.Transaction> transactions) {
    var startingBalance = StreamEx.of(transactions)
      .filter(tx -> tx.isBookedBefore(fromInclusive))
      .foldRight(BigDecimal.ZERO, BankAccount.Transaction::apply);

    var closingBalance = StreamEx.of(transactions)
      .filter(tx -> tx.isBookedDuring(fromInclusive, toInclusive))
      .foldLeft(startingBalance, (balance, tx) -> {
        var newBalance = tx.apply(balance);
        var newEntry = new Entry(tx.bookingTime(), tx.withdrawn(), tx.deposited(), newBalance);
        entries.add(newEntry);
        return newBalance;
      });

    this.startingBalance = new BalanceSnapshot(startingBalance, fromInclusive);
    this.closingBalance = new BalanceSnapshot(closingBalance, toInclusive);
  }

  public String json() {
    var root = new ObjectMapper().createObjectNode();

    root.putObject("startingBalance")
      .put("amount", startingBalance.balance)
      .put("date", startingBalance.date.format(ISO_DATE));

    root.putObject("closingBalance")
      .put("amount", closingBalance.balance)
      .put("date", closingBalance.date.format(ISO_DATE));

    var items = root.putArray("transactions");
    entries.forEach(entry -> items.addObject()
      .put("time", entry.time.format(ISO_LOCAL_DATE_TIME))
      .put("withdrawal", entry.withdrawal)
      .put("deposit", entry.deposit)
      .put("balance", entry.balance));

    return root.toPrettyString();
  }

  private record Entry(LocalDateTime time, BigDecimal withdrawal, BigDecimal deposit, BigDecimal balance) {
  }

  private record BalanceSnapshot(BigDecimal balance, LocalDate date) {
  }
}
