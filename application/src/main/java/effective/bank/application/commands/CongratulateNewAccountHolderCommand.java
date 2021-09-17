package effective.bank.application.commands;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Voidy;
import effective.bank.application.infra.spring.annotations.PrototypeComponent;
import effective.bank.domain.model.BankAccountRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static java.lang.String.format;

public record CongratulateNewAccountHolderCommand(String iban,
                                                  LocalDate date) implements Command<Voidy> {

    @PrototypeComponent
    record Handler(BankAccountRepository repo) implements Command.Handler<CongratulateNewAccountHolderCommand, Voidy> {

        @Override
        public Voidy handle(CongratulateNewAccountHolderCommand cmd) {
            var account = repo.getOne(cmd.iban);
            var message = format(
                    "Congratulations, %s. Thanks for using our services in %s",
                    account.holder().name(), cmd.date.getYear());
            System.out.println(message);
            return new Voidy();
        }
    }
}