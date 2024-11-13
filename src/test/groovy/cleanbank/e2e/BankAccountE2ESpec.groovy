package cleanbank.e2e

import cleanbank.e2e.commons.E2ESpec

class BankAccountE2ESpec extends E2ESpec {

  def "new bank account opening with congratulations"() {
    given:
    def iban = faker.finance().iban("EE")
    def someone = person()

    when:
    someone.appliesForBankAccount(iban)

    then:
    eventually {
      someone.shouldReceiveAnEmail {
        it.subject == "Heads up from Cleanbank"
        it.text == "Congratulations, $someone.firstName $someone.lastName! Your bank account $iban is ready. Thanks for using our services!"
      }
    }
  }

  def "attempt to apply for bank account with invalid iban"() {
    given:
    def invalidIban = "GX82WEST12345698765432"
    def someone = person()

    when:
    someone.appliesForBankAccount(invalidIban)

    then:
    someone.responses.last().status == 400
    someone.responses.last().json() == ["iban must be valid"]
  }


  def "attempt to grab an existing bank account"() {
    given:
    def iban = faker.finance().iban("EE")
    def someone = person()
    def someoneElse = person()

    when:
    someone.appliesForBankAccount(iban)

    and:
    someoneElse.appliesForBankAccount(iban)

    then:
    someone.responses.last().status == 200

    and:
    someoneElse.responses.last().status == 400
    someoneElse.responses.last().json() == ["iban is already taken"]
  }

}
