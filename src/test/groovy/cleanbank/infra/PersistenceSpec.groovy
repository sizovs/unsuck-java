package cleanbank.infra

import net.datafaker.Faker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
abstract class PersistenceSpec extends Specification {

  final Faker faker = new Faker()

  @Autowired
  PlatformTransactionManager txManager

  def concurrentWork = new CountDownLatch(1)
  def allWork = new CountDownLatch(2)

  def setupTransactional() {}

  def setup() {
    transactional {
      setupTransactional()
    }
  }

  def <T> T transactional(Closure<T> closure) {
    def tx = new TransactionTemplate(txManager)
    tx.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
    tx.execute(closure) as T
  }

  def concurrently(Closure work) {
    Thread.startVirtualThread {
      try {
        work()
      } finally {
        allWork.countDown()
      }
    }
  }

  def whileOngoing(Closure work) {
    concurrently {
      transactional {
        work()
        concurrentWork.await(10, TimeUnit.SECONDS)
      }
    }
  }

  def someoneCommits(Closure work) {
    concurrently {
      transactional {
        work()
      }
      concurrentWork.countDown()
    }
  }

  def afterAll(Closure work) {
    allWork.await(10, TimeUnit.SECONDS)
    transactional(work)
  }


}
