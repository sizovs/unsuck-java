package cleanbank.multimapping

import cleanbank.infra.EnableLogInterception
import cleanbank.infra.Logs
import cleanbank.infra.PersistenceSpec
import jakarta.persistence.*
import org.springframework.beans.factory.annotation.Autowired

@EnableLogInterception
class MultimappingSpec extends PersistenceSpec {

  @Autowired
  EntityManager em

  Logs logs

  def "with multimapping, each entity is cached separately"() {
    given:
    def foo = new Foo()
    def bar = new Bar()
    transactional {
      em.persist(foo)
      em.persist(bar)
    }
    when: "I read Foo and Bar twice"
    transactional {
      em.find(Foo, foo.id)
      em.find(Foo, foo.id)
      em.find(Bar, bar.id)
      em.find(Bar, bar.id)
    }

    then: "Hibernate should issue 2x selects – 1x per Foo, 1x per Bar"
    def selects = logs.findAll {
      it.message.contains('select') &&
        it.message.contains('test_multimapping')
    }
    selects.size() === 2
  }

  @MappedSuperclass
  static class Superclass {
    @Id
    UUID id = UUID.randomUUID()
  }

  @Entity
  @Table(name = "TEST_MULTIMAPPING")
  static class Foo extends Superclass {
    private String foo = "foo"
  }

  @Entity
  @Table(name = "TEST_MULTIMAPPING")
  static class Bar extends Superclass {
    private String bar = "bar"
  }

}
