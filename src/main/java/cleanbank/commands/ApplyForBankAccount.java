package cleanbank.commands;

import cleanbank.domains.accounts.BankAccount;
import cleanbank.domains.accounts.BankAccounts;
import cleanbank.domains.accounts.Iban;
import cleanbank.domains.accounts.WithdrawalLimits;
import cleanbank.domains.crm.Clients;
import cleanbank.infra.mail.Postman;
import cleanbank.infra.pipeline.Command;
import cleanbank.infra.pipeline.Command.Void;
import cleanbank.infra.spring.annotations.PrototypeScoped;
import cleanbank.infra.validation.Rules;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.SimpleMailMessage;

import java.math.BigDecimal;
import java.util.UUID;

import static cleanbank.infra.fp.Memoization.memoize;

public record ApplyForBankAccount(UUID clientId, String iban) implements Command<Void> {

  @PrototypeScoped
  static class Reaction implements Command.Reaction<ApplyForBankAccount, Void> {

    private final BankAccounts accounts;
    private final Iban.Uniqueness ibanUniqueness;
    private final WithdrawalLimits withdrawalLimits;

    Reaction(BankAccounts accounts, WithdrawalLimits withdrawalLimits) {
      this.accounts = accounts;
      this.withdrawalLimits = withdrawalLimits;
      this.ibanUniqueness = memoize(accounts::notExistsByIban)::once;
    }

    @Override
    public Void react(ApplyForBankAccount cmd) {
      new Rules()
        .define(cmd::clientId, ObjectUtils::allNotNull, "clientId must not be empty")
        .define(cmd::iban, StringUtils::isNotEmpty, "iban must not be empty", new Rules()
          .define(cmd::iban, Iban::isValid, "iban must be valid")
          .define(cmd::iban, ibanUniqueness::guaranteed, "iban is already taken")
        ).enforce();

      var iban = new Iban(cmd.iban(), ibanUniqueness);
      var account = new BankAccount(iban, cmd.clientId, withdrawalLimits);
      account.open();
      account.deposit(signupBonus());
      accounts.add(account);

      var congratulate = new CongratulateNewAccountOpening(cmd.clientId(), account.iban().toString());
      congratulate.later();

      return new Void();
    }

    private BigDecimal signupBonus() {
      return new BigDecimal("5.00");
    }
  }

  record CongratulateNewAccountOpening(UUID clientId, String iban) implements Command<Void> {

    @PrototypeScoped
    static class Reaction implements Command.Reaction<CongratulateNewAccountOpening, Void> {

      private final Clients clients;
      private final Postman postman;

      Reaction(Clients clients, Postman postman) {
        this.clients = clients;
        this.postman = postman;
      }

      @Override
      public Void react(CongratulateNewAccountOpening cmd) {
        var client = clients.findById(cmd.clientId());

        var mail = new SimpleMailMessage();
        mail.setFrom("hello@cleanbank.io");
        mail.setSubject("Heads up from Cleanbank");
        mail.setText("Congratulations, %s! Your bank account %s is ready. Thanks for using our services!".formatted(client.name(), cmd.iban()));

        mail.setTo(client.email());
        postman.deliver(mail);

        return new Void();
      }
    }
  }

}
