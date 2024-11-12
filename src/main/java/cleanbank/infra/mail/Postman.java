package cleanbank.infra.mail;

import org.springframework.mail.SimpleMailMessage;

import java.util.Collections;
import java.util.List;

public interface Postman {

  default List<SimpleMailMessage> deliveries() {
    return Collections.emptyList();
  }

  void deliver(SimpleMailMessage mail);

}
