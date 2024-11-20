package cleanbank.infra.pipeline;

import cleanbank.infra.locks.AdvisoryLock;
import cleanbank.infra.spring.annotations.PrototypeScoped;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public class ClusterOnce<C extends Command<R>, R> implements Command<Command.Void> {

  private final String taskId;
  private final C origin;

  public ClusterOnce(C origin) {
    this.taskId = origin.getClass().getSimpleName();
    this.origin = origin;
  }

  @PrototypeScoped
  static class Reaction<C extends Command<R>, R> implements Command.Reaction<ClusterOnce<C, R>, Void> {

    private final PlatformTransactionManager transactionManager;
    private final AdvisoryLock lock;

    Reaction(PlatformTransactionManager transactionManager, AdvisoryLock lock) {
      this.transactionManager = transactionManager;
      this.lock = lock;
    }

    @Override
    public Void react(ClusterOnce<C, R> cmd) {
      var tx = new TransactionTemplate(transactionManager);
      tx.setName(cmd.taskId);
      tx.executeWithoutResult(status -> {
        if (lock.acquire(cmd.taskId)) {
          cmd.origin.now();
        }
      });

      return new Void();
    }
  }

}
