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

  @PostMapping(value = "/clients")
  UUID post(@RequestBody RegisterAsClient register) {
    return register.now();
  }

  @GetMapping(value = "/clients/{id}")
  GetClientProfile.Profile post(@PathVariable UUID id) {
    var getClientProfile = new GetClientProfile(id);
    return getClientProfile.now();
  }

}
