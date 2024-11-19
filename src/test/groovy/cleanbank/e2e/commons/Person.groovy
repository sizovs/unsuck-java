package cleanbank.e2e.commons

import cleanbank.infra.mail.Postman
import net.datafaker.Faker
import org.springframework.mail.SimpleMailMessage
import org.springframework.test.web.servlet.MockMvc

import java.util.function.Predicate

class Person implements MockMvcTrait {

  Faker faker = new Faker()
  String firstName = faker.name().firstName()
  String lastName = faker.name().lastName()
  String personalId = faker.idNumber().ssnValid()
  String email = faker.internet().emailAddress()
  String ipAddress = faker.internet().ipV4Address()

  Optional<String> clientId = Optional.empty()

  MockMvc mvc
  Postman postman

  Person(MockMvc mvc, Postman postman) {
    this.mvc = mvc
    this.postman = postman
  }

  def registersAsClient() {
    String clientId = post("/clients", [
      firstName : firstName,
      lastName  : lastName,
      personalId: personalId,
      email     : email
    ]).json()

    assert clientId.length() === 36

    this.clientId = Optional.of(clientId)
  }

  def getsProfile() {
    get("/clients/${clientId.get()}", [:])
  }

  def appliesForBankAccount(iban) {
    registersAsClient()
    post("/bank-accounts", [
      clientId: clientId.get(),
      iban    : iban
    ])
  }

  def shouldReceiveEmail(Predicate<SimpleMailMessage> emailSpec) {
    postman.deliveries().any { it.to.contains(email) && emailSpec(it) }
  }

}
