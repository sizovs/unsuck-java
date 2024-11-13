package cleanbank.e2e.commons

import groovy.json.JsonSlurper
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

import static groovy.json.JsonOutput.toJson
import static org.springframework.http.MediaType.APPLICATION_JSON

class MockMvcExtensions {

  static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder builder, Map jsonAttributes) {
    builder.contentType(APPLICATION_JSON)
      .content(toJson(jsonAttributes))
  }

  static <T> T json(MockHttpServletResponse self) {
    new JsonSlurper().parseText(self.getContentAsString()) as T
  }

}
