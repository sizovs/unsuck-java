package cleanbank.infra.locks;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

// We use Postgres' advisory locks to ensure job executes once if multiple app instances are running.
// When using in H2, we switch to Java's in-memory locking, since it doesn't support advisory locks.

// As an alternative, universal implementation, you can use ShedLock https://github.com/lukas-krecan/ShedLock
// Or, just create table with a row per job, and select the row for update for the duration of the job.
@Component
public class Locking {

  private final Lock lock;

  Locking(Lock lock) {
    this.lock = lock;
  }

  public void runExclusive(String taskId, Runnable runnable) {
    try {
      lock.acquire(taskId);
      runnable.run();
    } finally {
      lock.release(taskId);
    }
  }

  interface Lock {
    void acquire(String taskId);

    void release(String taskId);
  }

  @Component
  @ConditionalOnClass(name = "org.h2.Driver")
  static class InMemoryLock implements Lock {

    private final Map<String, ReentrantLock> locks = new HashMap<>();

    @Override
    public void acquire(String taskId) {
      var lock = locks.computeIfAbsent(taskId, key -> new ReentrantLock());
      lock.lock();
    }

    @Override
    public void release(String taskId) {
      var lock = locks.get(taskId);
      lock.unlock();
    }
  }

  @Component
  @ConditionalOnClass(name = "org.postgresql.Driver")
  static class AdvisoryLock implements Lock {

    private final JdbcTemplate db;

    AdvisoryLock(JdbcTemplate db) {
      this.db = db;
    }

    public void acquire(String taskId) {
      db.update("SELECT pg_advisory_lock(?)", taskId.hashCode());
    }

    public void release(String taskId) {
      db.update("SELECT pg_advisory_unlock(?)", taskId.hashCode());
    }
  }
}
