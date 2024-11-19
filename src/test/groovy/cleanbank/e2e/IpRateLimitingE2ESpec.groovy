package cleanbank.e2e

import cleanbank.e2e.commons.E2ESpec

class IpRateLimitingE2ESpec extends E2ESpec {

  def "maximum 60 requests per minute from a single ip"() {
    given:
    def someone = person()
    def someoneElse = person()

    when:
    someone.registersAsClient()

    and:
    60.times { someone.getsProfile() }

    and:
    someoneElse.registersAsClient()

    // someone gets some of their requests throttled
    then:
    def successfulRequest = someone.responses.count { it.status == 200 }
    def throttledRequests = someone.responses.count { it.status == 429 }
    successfulRequest == 60
    throttledRequests == 1

    // someone else is still within the limits
    and:
    someoneElse.responses.last().status == 200
  }

}
