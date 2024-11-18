package cleanbank.infra.validation

import org.apache.commons.lang3.StringUtils
import spock.lang.Specification
import spock.lang.Subject

class RulesSpec extends Specification {

  static record Bean(String name, String country) {}

  @Subject
  def rules = new Rules()

  def "throws violations in rule declaration order"() {
    when:
    def bean = new Bean(name, country)
    rules
      .define(bean::name, StringUtils::isNotEmpty, "Name is empty")
      .define(bean::country, StringUtils::isNotEmpty, "Country is empty", nested ->
        nested.define(bean::country, StringUtils::isAllUpperCase, "Country '%s' must be uppercase"))
      .enforce()

    then:
    def e = thrown(Rules.Violations)
    e.violations() == violations

    where:
    name | country || violations
    ""   | ""      || ["Name is empty", "Country is empty"]
    ""   | "US"    || ["Name is empty"]
    "Ed" | ""      || ["Country is empty"]
    "Ed" | "us"    || ["Country 'us' must be uppercase"]
  }

  def "stays silent if all rules pass"() {
    when:
    rules
      .define("xx"::toString, StringUtils::isNotEmpty, "This will always pass")
      .enforce()

    then:
    noExceptionThrown()
  }


}
