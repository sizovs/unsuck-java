package cleanbank;

import cleanbank.domains.accounts.WithdrawalLimits;
import cleanbank.infra.jpa.BaseJpaRepository;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(WithdrawalLimits.class)
@EnableScheduling
@EnableJpaRepositories(repositoryBaseClass = BaseJpaRepository.class)
public class Application {

  static {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
