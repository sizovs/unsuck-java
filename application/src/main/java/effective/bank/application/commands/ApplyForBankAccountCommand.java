package effective.bank.application.commands;

import an.awesome.pipelinr.Command;
import effective.bank.application.infra.encrypt.Encryption;
import effective.bank.application.infra.pipeline.middlewares.resilience.RateLimited;
import effective.bank.application.infra.scheduling.Scheduler;
import effective.bank.application.infra.spring.annotations.PrototypeComponent;
import effective.bank.application.infra.validation.Validator;
import effective.bank.domain.model.*;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import org.springframework.core.env.Environment;

import static effective.bank.utils.TimeMachine.today;
import static java.time.Duration.ofSeconds;

public record ApplyForBankAccountCommand(
        String firstName,
        String lastName,
        String email,
        String iban
) implements Command<ApplyForBankAccountCommand.R>, RateLimited {

    @Override
    public Bandwidth bandwidth() {
        var maxCallsPerSecond = 50;
        var refillRate = Refill.greedy(10, ofSeconds(1));
        return Bandwidth.classic(maxCallsPerSecond, refillRate);
    }

    @PrototypeComponent
    static class Handler implements Command.Handler<ApplyForBankAccountCommand, R> {

        private final Environment env;
        private final BankAccountRepository repo;
        private final Scheduler scheduler;
        private final IbanUniqueness ibanUniqueness;

        Handler(Environment env, BankAccountRepository repo,
                Scheduler scheduler,
                IbanUniqueness ibanUniqueness) {
            this.env = env;
            this.repo = repo;
            this.scheduler = scheduler;
            this.ibanUniqueness = ibanUniqueness.memoized();
        }

        @Override
        public R handle(ApplyForBankAccountCommand cmd) {
            new Validator<ApplyForBankAccountCommand>()
                    .with(() -> cmd.firstName, v -> !v.isBlank(), "firstName is missing")
                    .with(() -> cmd.lastName, v -> !v.isBlank(), "lastName is missing")
                    .with(() -> cmd.email, v -> !v.isBlank(), "email is missing")
                    .with(() -> cmd.iban, v -> !v.isBlank(), "iban is missing", nested ->
                            nested
                                    .with(() -> cmd.iban, Iban::isValid, "iban is not valid")
                                    .with(() -> cmd.iban, ibanUniqueness::guaranteed, "iban is taken")
                    ).check(cmd);

            var iban = new Iban(cmd.iban, ibanUniqueness);
            var accountHolder = new AccountHolder(cmd.firstName, cmd.lastName, cmd.email);
            var withdrawalLimits = new WithdrawalLimits(env);
            var account = new BankAccount(iban, accountHolder, withdrawalLimits);
            account.open();
            account.deposit(openingBonus());
            repo.save(account);
            scheduler.schedule(new CongratulateNewAccountHolderCommand(account.iban() + "", today()));

            var saltyIban = Encryption.encrypt(account.iban() + "");
            return new R(saltyIban);
        }

        private Amount openingBonus() {
            return Amount.of("5.00");
        }
    }

    public record R(String saltyIban) {
    }

}



