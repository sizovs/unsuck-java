package cleanbank.acceptance

class BankAccountAcceptanceSpec extends AcceptanceSpec {

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
    def someoneGets = someone.appliesForBankAccount(invalidIban)

    then:
    someoneGets.status == 400
    someoneGets.json() == ["iban must be valid"]
  }


  def "attempt to grab an existing bank account"() {
    given:
    def iban = faker.finance().iban("EE")
    def someone = person()
    def someoneElse = person()

    when:
    def someoneGets = someone.appliesForBankAccount(iban)

    and:
    def someoneElseGets = someoneElse.appliesForBankAccount(iban)

    then:
    someoneGets.status == 200

    and:
    someoneElseGets.status == 400
    someoneElseGets.json() == ["iban is already taken"]
  }

}
