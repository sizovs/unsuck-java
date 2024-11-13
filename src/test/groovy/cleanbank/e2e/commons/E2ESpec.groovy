package cleanbank.e2e.commons

import cleanbank.infra.mail.Postman
import net.datafaker.Faker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@SpringBootTest
@AutoConfigureMockMvc
abstract class E2ESpec extends Specification {

  private def polling = new PollingConditions(timeout: 10, delay: 1)

  @Autowired
  protected MockMvc mvc

  @Autowired
  protected Postman postman

  protected Faker faker = new Faker()

  Person person() {
    new Person(mvc, postman)
  }

  void eventually(Closure conditions) {
    polling.eventually {
      assert conditions()
    }
  }

}
