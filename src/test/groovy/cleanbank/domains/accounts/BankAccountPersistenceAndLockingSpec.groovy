package cleanbank.domains.accounts

import cleanbank.infra.PersistenceSpec
import org.springframework.beans.factory.annotation.Autowired

class BankAccountPersistenceAndLockingSpec extends PersistenceSpec {

  @Autowired
  BankAccounts accounts

  String iban = faker.finance().iban("EE")

  @Override
  def setupTransactional() {
    def iban = new Iban(iban, Iban.Uniqueness.GUARANTEED)
    def clientId = UUID.randomUUID()
    def limits = new WithdrawalLimits(100.00, 1000.00)
    def account = new BankAccount(iban, clientId, limits)
    accounts.add(account)
  }

  def account() {
    accounts.findByIban(iban)
  }

  // If you remove optimistic locking, the test will fail.
  // Overriding the successful commit.
  def "I should not override someone else's successful commit"() {
    when: "I am trying to open a bank account"
    whileOngoing {
      account().open()
    }
    and: "...meanwhile someone else has successfully closed the account"
    someoneCommits {
      account().close(UnsatisfiedObligations.NONE)
    }

    then: "After we've both done, the account should be closed"
    afterAll {
      account().isClosed()
    }
  }

  // If you remove optimistic locking, the test will fail.
  // Withdrawing more (101$) than allowed by limit (100$)
  def "I should not break aggregate root's invariants"() {
    given: "Bank account is open and has some cash"
    transactional {
      account().open()
      account().deposit(1000.00)
    }
    and:
    account().withdrawalLimits().dailyLimit() == 100.00

    when: "I am withdrawing maximum cash permitted by a daily limit"
    whileOngoing {
      account().withdraw(100.00)
    }
    and: "...meanwhile someone else has withdrawn another dollar"
    someoneCommits {
      account().withdraw(1.00)
    }

    then: "After we've all done, my balance should decrease by one dollar (no more than allowed by the daily limit)"
    afterAll {
      println account().balance()
      account().balance() == 999.00
    }
  }

  // If you remove optimistic locking, and add @DynamicUpdate annotation to BankAccount, the test will fail.
  // Resulting in data mojito from two transactions – opened account and new limits.
  def "I should not partially override someone else's successful commit"() {
    def newLimits = new WithdrawalLimits(10000.00, 1000000.00)

    when: "I am trying to open a bank account"
    whileOngoing {
      account().open()
    }
    and: "...meanwhile someone has closed the account and lifted limits"
    someoneCommits {
      account().lift(newLimits)
      account().close(UnsatisfiedObligations.NONE)
    }

    then: "After we've both done, the account should be closed and limits lifted"
    afterAll {
      account().isClosed() && account().withdrawalLimits() == newLimits
    }
  }

  // This is just to make sure transactions are ordered properly when read from the database.
  def "Transactions should be ordered by insertion order"() {
    when: "I save a bunch of transactions"
    transactional {
      account().open()
      account().deposit(1.00)
      account().deposit(2.00)
      account().deposit(3.00)
    }
    then: "Transactions should be returned in the insertion order"
    transactional {
      account().transactions().collect { it.deposited() } == [1.00, 2.00, 3.00]
    }
  }
}
