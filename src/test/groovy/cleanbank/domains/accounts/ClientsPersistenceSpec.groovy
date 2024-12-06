package cleanbank.domains.accounts

import cleanbank.domains.crm.Client
import cleanbank.domains.crm.Clients
import cleanbank.infra.EnableLogInterception
import cleanbank.infra.Logs
import cleanbank.infra.PersistenceSpec
import org.springframework.beans.factory.annotation.Autowired

@EnableLogInterception
class ClientsPersistenceSpec extends PersistenceSpec {

  @Autowired
  Clients clients

  Logs logs

  String personalId = faker.idNumber().singaporeanUin()

  def setupTransactional() {
    def client = new Client(personalId, "Dwayne", "Johnson", "dwayne@hollywood.com")
    clients.add(client)
  }

  def "issues only one select when queried by natural id"() {
    when:
    transactional {
      clients.findByNaturalId(personalId)
      clients.findByNaturalId(personalId)
      clients.findByNaturalId(personalId)
    }
    def selects = logs.findAll {
      it.message.contains('select') &&
        it.message.contains('personal_id=?')
    }
    then:
    selects.size() === 1
  }

}
