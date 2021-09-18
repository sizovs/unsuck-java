package effective.bank.domain.model

import com.github.javafaker.Faker
import org.springframework.beans.factory.annotation.Autowired

class BankAccountLockingSpec extends PersistenceSpecification {

    @Autowired
    BankAccountRepository repo

    def faker = new Faker()

    def accountHolder = new AccountHolder(faker.name().firstName(), faker.name().lastName(), faker.internet().emailAddress())

    def defaultLimits = new WithdrawalLimits(Amount.of(100.00), Amount.of(1000.00))

    def iban = faker.finance().iban("EE")

    @Override
    def setupTransactional() {
        def uniqueIban = new Iban(iban, { true } as IbanUniqueness)
        def account = new BankAccount(uniqueIban, accountHolder, defaultLimits)
        account.open()
        repo.save(account)
    }

    def account() {
        repo.getOne(iban)
    }

    def "I should not override someone else's successful commit"() {
        when: "I am trying to suspend a bank account, while someone has successfully closed the account"
        whileIamDoing {
            account().suspend()
        }
        someoneElseCompletes {
            account().close(UnsatisfiedObligations.NONE)
        }

        then: "After we've both done, the account should remain closed"
        afterAll {
            account().isClosed()
        }
    }

    // this test fails when you add @DynamicUpdate annotation to BankAccount while optimistic locking is off.
    def "I should not partially override someone else's successful commit"() {
        def newLimits = new WithdrawalLimits(Amount.of(10000.00), Amount.of(1000000.00))

        when: "I am trying to suspend a bank account, while someone else has closed the account and lifted limits"
        whileIamDoing {
            account().suspend()
        }
        someoneElseCompletes {
            account().lift(newLimits)
            account().close(UnsatisfiedObligations.NONE)
        }

        then: "After we've both done, the account should be closed and limits lifted"
        afterAll {
            account().isClosed() && account().withdrawalLimits() == newLimits
        }
    }

    def "I should not break aggregate root's invariants"() {
        def cash = Amount.of(1000.00)
        def oneDollar = Amount.of(1.00)

        given: "Bank account has some cash"
        transactional {
            account().deposit(cash)
        }

        when: "I am withdrawing maximum cash permitted by a daily limit. Meanwhile someone else withdraws 1 dollar more"
        whileIamDoing {
            account().withdraw(Amount.of(100.00))
        }
        someoneElseCompletes {
            account().withdraw(oneDollar)
        }

        then: "After we've all done, my balance should decrease no more than allowed by the daily limit"
        afterAll {
            println(account().balance())
            account().balance() == Amount.of(999.00)
        }
    }

}
