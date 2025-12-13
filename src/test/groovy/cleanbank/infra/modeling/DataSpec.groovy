package cleanbank.infra.modeling

import com.google.common.testing.EqualsTester
import spock.lang.Specification

class DataSpec extends Specification {

  static class UserProfile extends Data {
    String email
    String name
  }

  def "equals and hashCode should include all fields"() {
    when:
    // same
    new EqualsTester()
      .addEqualityGroup(
        new UserProfile(email: "bob@gmail.com", name: "Bob"),
        new UserProfile(email: "bob@gmail.com", name: "Bob"),
      )
    // different
      .addEqualityGroup(new UserProfile(email: "tom@gmail.com", name: "Tom"))
      .testEquals()

    then:
    noExceptionThrown()
  }

  def "toString should include all fields"() {
    given:
    var profile = new UserProfile(email: "bob@gmail.com", name: "Bob")

    expect:
    profile.toString() == "[email=bob@gmail.com,name=Bob]"
  }


}
