package effective.bank.domain.model

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
abstract class PersistenceSpecification extends spock.lang.Specification {

    @Autowired
    PlatformTransactionManager txManager

    def someoneElseDone = new CountDownLatch(1)
    def weHaveBothDone = new CountDownLatch(1)

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

    def whileIamDoing(Closure closure) {
        Thread.start {
            transactional {
                closure()
                someoneElseDone.await(10, TimeUnit.SECONDS)
            }
            weHaveBothDone.countDown()
        }
    }

    def someoneElseCompletes(Closure closure) {
        Thread.start {
            transactional(closure)
            someoneElseDone.countDown()
            weHaveBothDone.countDown()
        }
    }

    def afterAll(Closure closure) {
        weHaveBothDone.await()
        transactional(closure)
    }

    @SpringBootApplication
    static class DumbApp {

    }

}