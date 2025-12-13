package cleanbank.infra.spring.mvc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ClientIp {

  private static final ScopedValue<String> CLIENT_IP = ScopedValue.newInstance();

  public boolean isAvailable() {
    return CLIENT_IP.isBound();
  }

  @Override
  public String toString() {
    return CLIENT_IP.orElseThrow(() -> new RuntimeException("Client ip is not available"));
  }

  @Component
  static class RequestFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain) {
      ScopedValue.where(CLIENT_IP, request.getRemoteAddr()).run(() -> {
        try {
          chain.doFilter(request, response);
        } catch (IOException | ServletException e) {
          throw new RuntimeException(e);
        }
      });
    }
  }

}
