package cleanbank.domains.accounts


import cleanbank.infra.time.TimeMachine
import net.datafaker.Faker
import spock.lang.Specification
import spock.lang.Subject
import spock.util.time.MutableClock

import static cleanbank.infra.time.TimeMachine.today
import static java.time.Duration.ofDays
import static java.time.Instant.EPOCH
import static java.time.ZoneOffset.UTC

class BankAccountSpec extends Specification {

  def clock = new MutableClock(EPOCH, UTC)

  def faker = new Faker()

  def clientId = UUID.randomUUID()

  def limits = new WithdrawalLimits(100.00, 1000.00)

  def iban = new Iban(faker.finance().iban("EE"), Iban.Uniqueness.GUARANTEED)

  @Subject
  BankAccount account = new BankAccount(iban, clientId, limits)

  def setup() {
    TimeMachine.set(clock)
  }

  def "can be closed"() {
    given: "Account is open"
    account.open()
    assert account.isOpen()

    when: "I try to close the account"
    account.close(UnsatisfiedObligations.NONE)

    then: "Account must close"
    account.isClosed()
  }

  def "cannot be closed if client has unsatisfied obligations"() {
    given: "I have some unsatisfied obligations"
    def unsatisfiedObligations = { true } as UnsatisfiedObligations

    when: "I close my account"
    account.close(unsatisfiedObligations)

    then: "I get an error"
    def e = thrown(IllegalStateException)
    e.message == "Unsatisfied obligations exist."
  }

  def "can be deposited"() {
    given: "account is open"
    account.open()

    and: "I am out of cash"
    assert account.balance() == 0.00

    when: "I deposit some cash"
    def tx = account.deposit(100.00)

    then: "A deposit transaction should be created"
    tx.isDeposit()
    tx.deposited() == 100.00
    tx.withdrawn() == 0.00
  }

  def "can be withdrawn"() {
    given: "account is open"
    account.open()

    and: "I have some cash"
    account.deposit(100.00)
    assert account.balance() == 100.00

    when: "I withdraw it"
    def tx = account.withdraw(100.00)

    then: "A withdrawal transaction should be created"
    tx.isWithdrawal()
    tx.withdrawn() == 100.00
    tx.deposited() == 0.00
    account.publishedEvents.any { it == new WithdrawalHappened(account.iban(), 100.00) }
  }

  def "cannot be withdrawn when closed"() {
    given: "Account is closed"
    account.close(UnsatisfiedObligations.NONE)

    when: "I try to withdraw my cash"
    account.withdraw(100.00)

    then: "I get an error"
    def e = thrown(IllegalStateException)
    e.message == "Account is not open."
  }

  def "cannot be deposited when closed"() {
    given: "Account is closed"
    account.close(UnsatisfiedObligations.NONE)

    when: "I try to deposit some cash"
    account.deposit(100.00)

    then: "I get an error"
    def e = thrown(IllegalStateException)
    e.message == "Account is not open."
  }

  def "cannot be withdrawn when insufficient funds"() {
    given: "Account is open"
    account.open()

    and: "I am out of money"
    assert account.balance() == 0.00

    when: "I withdraw some cash"
    account.withdraw(1.00)

    then: "I get an error"
    def e = thrown(IllegalStateException)
    e.message == "Insufficient funds."
  }

  def "cannot be withdrawn when daily limit exceeded"() {
    given: "Account is open"
    account.open()

    and: "I have some spare cash"
    account.deposit(1000.00)

    when: "I withdraw more than allowed by daily limit"
    account.withdraw(101.00)

    then: "I get an error"
    def e = thrown(IllegalStateException)
    e.message == "Daily withdrawal limit reached."
  }

  def "cannot be withdrawn when monthly limit exceeded"() {
    given: "Account is open"
    account.open()

    and: "I have some spare cash"
    account.deposit(2000.00)

    when: "I withdraw more than allowed by monthly limit"
    account.withdraw(1001.00)

    then: "I get an error"
    def e = thrown(IllegalStateException)
    e.message == "Monthly withdrawal limit reached."
  }


  def "publishes a BankAccountOpened event when opened"() {
    when: "I try to open a bank account"
    account.open()

    then: "An event gets published"
    account.publishedEvents.any { it == new BankAccountOpened(account.iban(), today()) }
  }

  def "calculates a balance"() {
    given: "Account is open"
    account.open()

    when: "I try to deposit and withdraw some cash"
    account.deposit(100.00)
    account.withdraw(20.50)
    account.withdraw(20.00)
    def balance = account.balance()

    then: "My balance shows a sum of all transactions"
    balance == 59.50
  }

  def "provides statement for a given time interval"() {
    given: "Account is open"
    account.open()

    and: "I perform a series of deposits and withdrawals on different days"

    clock + ofDays(1)
    account.deposit(100.00)

    clock + ofDays(1)
    def from = today()
    account.deposit(99.00)

    clock + ofDays(1)
    def to = today()
    account.withdraw(98.00)

    clock + ofDays(1)
    account.withdraw(2.00)

    when: "I ask for a bank statement"
    def actual = account.statement(from, to).json()

    then: "I should see all operations as a nicely formatted JSON"
    def expected = """
                  {
                    "startingBalance": {
                      "date": "1970-01-03",
                      "amount": 100.00
                    },
                    "closingBalance": {
                      "date": "1970-01-04",
                      "amount": 101.00
                    },
                    "transactions": [
                      {
                        "time": "1970-01-03T00:00:00",
                        "deposit": 99.00,
                        "withdrawal": 0.00,
                        "balance": 199.00
                      },
                      {
                        "time": "1970-01-04T00:00:00",
                        "deposit": 0.00,
                        "withdrawal": 98.00,
                        "balance": 101.00
                      }
                    ]
                   }
                """

    assert expected.json() == actual.json()
  }

}
