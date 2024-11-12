package cleanbank.infra.mail


import groovy.util.logging.Slf4j
import org.springframework.context.annotation.Primary
import org.springframework.mail.SimpleMailMessage
import org.springframework.stereotype.Component

@Component
@Primary
@Slf4j
class MockPostman implements Postman {

  private final deliveries = new ArrayList<SimpleMailMessage>()

  @Override
  void deliver(SimpleMailMessage mail) {
    log.info("Email delivered {}", mail)
    deliveries.add(mail)
  }

  @Override
  List<SimpleMailMessage> deliveries() {
    deliveries
  }

}
