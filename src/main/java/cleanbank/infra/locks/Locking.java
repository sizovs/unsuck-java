package cleanbank.infra.locks;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization;

// We use Postgres' advisory locks to ensure job executes once if multiple app instances are running.
// When using in H2, we switch to Java's in-memory locking, since it doesn't support advisory locks natively.

// As an alternative, universal implementation, you can use ShedLock https://github.com/lukas-krecan/ShedLock
// Or, just create table with a row per job, and select the row for update for the duration of the job.
@Component
public class Locking {

  private final Lock lock;
  private final PlatformTransactionManager transactionManager;

  Locking(Lock lock, PlatformTransactionManager transactionManager) {
    this.lock = lock;
    this.transactionManager = transactionManager;
  }

  public void runExclusive(String taskId, Runnable runnable) {
    var tx = new TransactionTemplate(transactionManager);
    tx.setName(taskId);
    tx.executeWithoutResult(status -> {
      if (lock.acquire(taskId)) {
        runnable.run();
      }
    });
  }

  interface Lock {
    boolean acquire(String taskId);
  }

  @Component
  static class AdvisoryLock implements Lock {

    private final JdbcTemplate db;

    AdvisoryLock(JdbcTemplate db) {
      this.db = db;
    }

    public boolean acquire(String taskId) {
      var acquired = db.queryForObject("SELECT pg_try_advisory_xact_lock(?)", Boolean.class, taskId.hashCode());
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
        db.execute("CREATE ALIAS pg_try_advisory_xact_lock FOR \"%s.acquire\"".formatted(Emulator.class.getName()));
      }

      public static boolean acquire(int lockId) {
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
}
