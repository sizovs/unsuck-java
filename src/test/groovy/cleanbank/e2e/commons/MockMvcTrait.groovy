package cleanbank.e2e.commons

import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

trait MockMvcTrait {

  List<MockHttpServletResponse> responses = []

  MockHttpServletResponse get(String uri, Map jsonAttributes) {
    def request = get(uri)
      .json(jsonAttributes)
      .remoteAddress(ipAddress)

    def response = mvc.perform(request)
      .andReturn()
      .getResponse()

    responses << response
    response
  }


  MockHttpServletResponse post(String uri, Map jsonAttributes) {
    def request = post(uri)
      .json(jsonAttributes)
      .remoteAddress(ipAddress)

    def response = mvc.perform(request)
      .andReturn()
      .getResponse()

    responses << response
    response
  }

  abstract MockMvc getMvc()

  abstract String getIpAddress()


}
