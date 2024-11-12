package cleanbank;

import cleanbank.commands.ApplyForBankAccount;
import cleanbank.commands.RegisterAsClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
class WebApi {

  @PostMapping("/bank-accounts")
  void post(@RequestBody ApplyForBankAccount applyForBankAccount) {
    applyForBankAccount.now();
  }

  @PostMapping(value = "/clients")
  UUID post(@RequestBody RegisterAsClient register) {
    return register.now();
  }

}
