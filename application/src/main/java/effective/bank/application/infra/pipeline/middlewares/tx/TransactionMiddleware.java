package effective.bank.application.infra.pipeline.middlewares.tx;

import an.awesome.pipelinr.Command;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@Order(15)
class TransactionMiddleware implements Command.Middleware {

  private final PlatformTransactionManager txManager;

  TransactionMiddleware(PlatformTransactionManager txManager) {
    this.txManager = txManager;
  }

  @Override
  public <R, C extends Command<R>> R invoke(C command, Next<R> next) {
    var tx = new TransactionTemplate(txManager);
    tx.setName("Tx for " + command.getClass().getSimpleName());
    tx.setReadOnly(command instanceof ReadOnly);
    return tx.execute(status -> next.invoke());
  }
}