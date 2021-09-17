package effective.bank;

import an.awesome.pipelinr.Pipeline;
import effective.bank.application.commands.ApplyForBankAccountCommand;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EffectiveBank implements CommandLineRunner {

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    Pipeline pipeline;

    EffectiveBank(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public static void main(String[] args) {
        SpringApplication.run(EffectiveBank.class, args);
    }

    @Override
    public void run(String... args) {
        var command = new ApplyForBankAccountCommand("Chuck", "Norris", "chuck@norris.net", "AD1400080001001234567890");
        command.execute(pipeline);
    }
}
