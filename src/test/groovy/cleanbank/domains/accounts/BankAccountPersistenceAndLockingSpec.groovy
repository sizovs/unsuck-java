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

  def "I should not override someone else's successful commit"() {
    when: "I am trying to open a bank account, while someone has successfully closed the account"
    whileOngoing {
      account().open()
    }
    someoneCommits {
      account().close(UnsatisfiedObligations.NONE)
    }

    then: "After we've both done, the account should be closed"
    afterAll {
      account().isClosed()
    }
  }

  // this test fails when you add @DynamicUpdate annotation to BankAccount while optimistic locking is off.
  def "I should not partially override someone else's successful commit"() {
    def newLimits = new WithdrawalLimits(10000.00, 1000000.00)

    when: "I am trying to open a bank account, while someone has closed the account and lifted limits"
    whileOngoing {
      account().open()
    }
    someoneCommits {
      account().lift(newLimits)
      account().close(UnsatisfiedObligations.NONE)
    }

    then: "After we've both done, the account should be closed and limits lifted"
    afterAll {
      account().isClosed() && account().withdrawalLimits() == newLimits
    }
  }

  def "I should not break aggregate root's invariants"() {
    given: "Bank account is open and has some cash"
    transactional {
      account().open()
      account().deposit(1000.00)
    }

    when: "I am withdrawing maximum cash permitted by a daily limit. Meanwhile someone else has withdrawn another dollar"
    whileOngoing {
      account().withdraw(100.00)
    }
    someoneCommits {
      account().withdraw(1.00)
    }

    then: "After we've all done, my balance should decrease by one dollar (no more than allowed by the daily limit)"
    afterAll {
      account().balance() == 999.00
    }
  }

  def "Transactions should be ordered by insertion order"() {
    when: "I save a bunch of transactions"
    transactional {
      account().open()
      account().deposit(1.00)
      account().deposit(2.00)
      account().deposit(3.00)
    }
    then: "Then should be read back in the same order"
    transactional {
      account().transactions().collect { it.deposited() } == [1.00, 2.00, 3.00]
    }
  }

}
