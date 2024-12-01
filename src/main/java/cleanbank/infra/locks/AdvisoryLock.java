package cleanbank.infra.locks;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization;

// We use Postgres' advisory locks to ensure a job executes once if multiple app instances are running.
// When using H2, we switch to Java's in-memory locking, since it doesn't support advisory locks natively.

// As an alternative, universal implementation, you can use ShedLock https://github.com/lukas-krecan/ShedLock
// Or, just create table with a row per job, and select the row for update for the duration of the job.
@Component
public class AdvisoryLock {

  private final JdbcTemplate db;

  public AdvisoryLock(JdbcTemplate db) {
    this.db = db;
  }

  public boolean tryAcquire(int lockId) {
    var acquired = db.queryForObject("SELECT pg_try_advisory_xact_lock(?)", Boolean.class, lockId);
    return Boolean.TRUE.equals(acquired);
  }

  @Component
  @ConditionalOnClass(name = "org.h2.Driver")
  public static class Emulator {

    private static final ConcurrentHashMap<Integer, ReentrantLock> locks = new ConcurrentHashMap<>();

    private final JdbcTemplate db;

    Emulator(JdbcTemplate db) {
      this.db = db;
    }

    @PostConstruct
    public void setup() {
      db.execute("CREATE ALIAS pg_try_advisory_xact_lock FOR \"%s.tryAcquire\"".formatted(Emulator.class.getName()));
    }

    public static boolean tryAcquire(int lockId) {
      var lock = locks.computeIfAbsent(lockId, key -> new ReentrantLock());
      var acquired = lock.tryLock();
      releaseAfterTxCompletion(lock);
      return acquired;
    }

    private static void releaseAfterTxCompletion(ReentrantLock lock) {
      registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCompletion(int status) {
          if (lock.isHeldByCurrentThread()) {
            lock.unlock();
          }
        }
      });
    }
  }
}
