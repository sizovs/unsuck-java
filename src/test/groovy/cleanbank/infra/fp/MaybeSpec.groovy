package cleanbank.infra.fp

import com.google.common.testing.EqualsTester
import spock.lang.Specification

class MaybeSpec extends Specification {

  class Boom extends RuntimeException {
  }

  class MegaBoom extends RuntimeException {
    MegaBoom(Throwable cause) {
      super(cause)
    }
  }

  def value = "value"
  def exception = new Boom()

  def "of() throws NPE if value is null"() {
    when:
    Maybe.of(null)
    then:
    def e = thrown(NullPointerException)
    e.message === "Value cannot be null"
  }

  def "of() throws NPE if exception is null"() {
    when:
    Maybe.of((Throwable) null)
    then:
    def e = thrown(NullPointerException)
    e.message === "Exception cannot be null"
  }

  def "isPresent returns true if value exists, and false if exceptional"() {
    expect:
    Maybe.of(value).isPresent()
    !Maybe.of(exception).isPresent()
  }

  def "isEmpty returns true if exceptional, and false if value exists"() {
    expect:
    Maybe.of(exception).isEmpty()
    !Maybe.of(value).isEmpty()
  }

  def "ifPresent() runs only if value exists"() {
    given:
    def callOfPresent = false
    def callOfAbsent = false

    when:
    Maybe.of(value).ifPresent { callOfPresent = true }
    Maybe.of(exception).ifPresent { callOfAbsent = true }

    then:
    callOfPresent
    !callOfAbsent
  }

  def "map() maps the value"() {
    expect:
    Maybe.of("hello").map { it -> it + " world" }.orElseThrow() == 'hello world'
  }

  def "map() returns itself if exceptional"() {
    when:
    def maybe = Maybe.of(exception)

    then:
    maybe == maybe.map { it -> it + " whatever " }
  }

  def "flatMap() maps the value"() {
    expect:
    Maybe.of("hello").flatMap { it -> Maybe.of(it + " world") }.orElseThrow() == 'hello world'
  }

  def "flatMap() returns itself if exceptional"() {
    when:
    def maybe = Maybe.of(exception)

    then:
    maybe == maybe.flatMap { it -> Maybe.of("whatever") }
  }

  def "orElse() returns itself, or new value if exceptional"() {
    expect:
    Maybe.of(value).orElse("newValue") == value
    Maybe.of(exception).orElse(value) == value
  }

  def "orElseGet() returns itself, or new value if exceptional"() {
    expect:
    Maybe.of(value).orElseGet({ "newValue" }) == value
    Maybe.of(exception).orElseGet({ value }) == value
  }

  def "orElseThrow() returns itself if value exists"() {
    expect:
    Maybe.of(value).orElseThrow() === value
    Maybe.of(value).orElseThrow(it -> new MegaBoom(it)) === value
  }

  def "orElseThrow() throws if exceptional"() {
    when:
    Maybe.of(exception).orElseThrow()

    then:
    def e = thrown(Boom)
    e == exception
  }

  def "orElseThrow() supports exception wrapping"() {
    when:
    Maybe.of(exception).orElseThrow(it -> new MegaBoom(it))

    then:
    def e = thrown(MegaBoom)
    e.cause == exception
  }

  def "toString() delegates to the underlying value"() {
    expect:
    Maybe.of(value).toString() == value
    Maybe.of(exception).toString() == exception.toString()
  }

  def "equals() and hashCode() delegate to the underlying value"() {
    when:
    new EqualsTester()
      .addEqualityGroup(Maybe.of(value), Maybe.of(value))
      .addEqualityGroup(Maybe.of(exception), Maybe.of(exception))
      .addEqualityGroup(Maybe.of("otherValue"))
      .addEqualityGroup(Maybe.of(new RuntimeException()))
      .testEquals()
    then:
    noExceptionThrown()
  }

}
