package cleanbank.infra.modeling


import com.google.common.testing.EqualsTester
import spock.lang.Specification

class DataSpec extends Specification {

  def "includes all fields in equals and hashCode"() {
    expect:
    new EqualsTester()
      .addEqualityGroup(new Bean("A", 1), new Bean("A", 1))
      .addEqualityGroup(new Bean("A", 2), new Bean("A", 2))
      .addEqualityGroup(new Bean("B", 1), new Bean("B", 1))
      .addEqualityGroup(new Bean(null, null), new Bean(null, null))
      .testEquals();
  }


  def "includes all fields in toString"() {
    expect:
    new Bean("Hello", 1960).toString() == "DataSpec.Bean[another=1960,one=Hello]"

    and:
    new Bean("", 0).toString() == "DataSpec.Bean[another=0,one=]"
  }

  static class Bean extends Data {
    String one
    Integer another

    Bean(String one, Integer another) {
      this.one = one;
      this.another = another;
    }
  }

}
