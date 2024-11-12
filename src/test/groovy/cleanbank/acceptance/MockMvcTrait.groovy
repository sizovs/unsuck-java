package cleanbank.acceptance

import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

trait MockMvcTrait {

  MockHttpServletResponse post(String uri, Map jsonAttributes) {
    def request = post(uri)
      .json(jsonAttributes)
      .remoteAddress(ipAddress)

    mvc.perform(request)
      .andReturn()
      .getResponse()
  }

  abstract MockMvc getMvc()

  abstract String getIpAddress()


}
