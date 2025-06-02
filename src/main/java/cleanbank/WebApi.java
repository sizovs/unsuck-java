package cleanbank;

import cleanbank.commands.ApplyForBankAccount;
import cleanbank.commands.GetClientProfile;
import cleanbank.commands.RegisterAsClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
class WebApi {

  @PostMapping("/bank-accounts")
  void post(@RequestBody ApplyForBankAccount applyForBankAccount) {
    applyForBankAccount.now();
  }

  @PostMapping("/clients")
  UUID post(@RequestBody RegisterAsClient register) {
    return register.now();
  }

  @GetMapping("/clients/{id}")
  GetClientProfile.Profile post(@PathVariable UUID id) {
    return new GetClientProfile(id).now();
  }

}
