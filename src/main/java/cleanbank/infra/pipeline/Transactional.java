package cleanbank.infra.pipeline;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

class Transactional implements Now {

  private final PlatformTransactionManager txManager;
  private final Now origin;

  Transactional(PlatformTransactionManager txManager, Now origin) {
    this.txManager = txManager;
    this.origin = origin;
  }

  @Override
  public <C extends Command<R>, R> R execute(C command) {
    var tx = new TransactionTemplate(txManager);
    tx.setName("Tx for " + command.getClass().getSimpleName());
    tx.setReadOnly(command instanceof ReadOnly);
    return tx.execute(status -> origin.execute(command));
  }
}
