package effective.bank.domain.model

import com.google.common.testing.EqualsTester
import effective.bank.domain.model.ValueObject

class ValueObjectSpec extends spock.lang.Specification {

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
        new Bean("Hello", 1960).toString() == "ValueObjectSpec.Bean[one=Hello,another=1960]"

        and:
        new Bean("", 0).toString() == "ValueObjectSpec.Bean[one=,another=0]"
    }

    static class Bean extends ValueObject {
        String one
        Integer another
        Bean(String one, Integer another) {
            this.one = one;
            this.another = another;
        }
    }

}
