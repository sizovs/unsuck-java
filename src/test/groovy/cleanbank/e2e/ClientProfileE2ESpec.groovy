package cleanbank.e2e

import cleanbank.e2e.commons.E2ESpec

class ClientProfileE2ESpec extends E2ESpec {

  def "get user profile"() {
    given:
    def someone = person()

    when:
    someone.registersAsClient()

    and:
    someone.getsProfile()

    then:
    someone.responses.last().json() == [
      email: someone.email,
      firstName: someone.firstName,
      lastName: someone.lastName
    ]
  }


}
