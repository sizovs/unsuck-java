package cleanbank.infra.locks;

import jakarta.annotation.PostConstruct;
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
  static class AdvisoryLock implements Lock {

    private final JdbcTemplate db;

    AdvisoryLock(JdbcTemplate db) {
      this.db = db;
    }

    public void acquire(String taskId) {
      db.execute("SELECT pg_advisory_lock(%s)".formatted(taskId.hashCode()));
    }

    public void release(String taskId) {
      db.execute("SELECT pg_advisory_unlock(%s)".formatted(taskId.hashCode()));
    }

    @Component
    @ConditionalOnClass(name = "org.h2.Driver")
    public static class Emulator {

      private static final Map<Integer, ReentrantLock> locks = new HashMap<>();
      private final JdbcTemplate db;

      Emulator(JdbcTemplate db) {
        this.db = db;
      }

      @PostConstruct
      public void setup() {
        db.update("CREATE ALIAS pg_advisory_lock FOR \"%s.acquire\"".formatted(Emulator.class.getName()));
        db.update("CREATE ALIAS pg_advisory_unlock FOR \"%s.release\"".formatted(Emulator.class.getName()));
      }

      public static void acquire(int lockId) {
        var lock = locks.computeIfAbsent(lockId, key -> new ReentrantLock());
        lock.lock();
      }

      public static void release(int lockId) {
        var lock = locks.get(lockId);
        lock.unlock();
      }
    }
  }
}
