package cleanbank.e2e

import cleanbank.e2e.commons.E2ESpec

class ClientProfileE2ESpec extends E2ESpec {

  def "populated user profile"() {
    given:
    def someone = person()

    when:
    someone.registersAsClient()

    and:
    someone.getsProfile()

    then:
    Map profile = someone.responses.last().json()
    profile.email == someone.email
    profile.firstName == someone.firstName
    profile.lastName == someone.lastName
  }


}
