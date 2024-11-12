package cleanbank.infra.spring.mvc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class ClientIp {

  private static final ThreadLocal<Optional<String>> CLIENT_IP = InheritableThreadLocal.withInitial(Optional::empty);

  public boolean isAvailable() {
    return CLIENT_IP.get().isPresent();
  }

  @Override
  public String toString() {
    return CLIENT_IP.get().orElseThrow(() -> new RuntimeException("Client ip is not available"));
  }

  @Component
  static class RequestFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
      throws ServletException, IOException {
      try {
        var ip = Optional.of(request.getRemoteAddr());
        CLIENT_IP.set(ip);
        chain.doFilter(request, response);
      } finally {
        CLIENT_IP.remove();
      }
    }
  }

}
