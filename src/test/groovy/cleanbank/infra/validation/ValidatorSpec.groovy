package cleanbank.infra.validation

import org.apache.commons.lang3.StringUtils
import spock.lang.Specification
import spock.lang.Subject

class ValidatorSpec extends Specification {

  static record Bean(String name, String country) {}

  @Subject
  def validator = new Validator()

  def "throws exception with all validation errors in rule declaration order"() {
    when:
    def bean = new Bean(name, country)
    validator
      .with(bean::name, StringUtils::isNotEmpty, "Name is empty")
      .with(bean::country, StringUtils::isNotEmpty, "Country is empty", nested ->
        nested.with(bean::country, StringUtils::isAllUpperCase, "Country must be uppercase"))
      .check(bean)

    then:
    def e = thrown(Validator.ValidationException)
    e.violations() == errors

    where:
    name | country || errors
    ""   | ""      || ["Name is empty", "Country is empty"]
    ""   | "US"    || ["Name is empty"]
    "Ed" | ""      || ["Country is empty"]
    "Ed" | "us"    || ["Country must be uppercase"]
  }

  def "doesn't throw if validation passes"() {
    when:
    validator
      .with(String::toString, StringUtils::isNotEmpty, "This will always pass")
      .check("not empty string")

    then:
    noExceptionThrown()
  }


}
