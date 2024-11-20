package cleanbank.infra.pipeline;

import cleanbank.infra.locks.AdvisoryLock;
import cleanbank.infra.spring.annotations.PrototypeScoped;

public class ClusterOnce<C extends Command<R>, R> implements Command<Command.Void> {

  private final String taskId;
  private final C origin;

  public ClusterOnce(C origin) {
    this.taskId = origin.getClass().getSimpleName();
    this.origin = origin;
  }

  @PrototypeScoped
  static class Reaction<C extends Command<R>, R> implements Command.Reaction<ClusterOnce<C, R>, Void> {

    private final AdvisoryLock lock;

    Reaction(AdvisoryLock lock) {
      this.lock = lock;
    }

    @Override
    public Void react(ClusterOnce<C, R> cmd) {
      if (lock.acquire(cmd.taskId)) {
        cmd.origin.now();
      }

      return new Void();
    }
  }

}
