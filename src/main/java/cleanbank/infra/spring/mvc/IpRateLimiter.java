package cleanbank.infra.spring.mvc;

import cleanbank.infra.pipeline.Command;
import cleanbank.infra.pipeline.RateLimiter;
import io.github.bucket4j.Bandwidth;
import org.springframework.stereotype.Component;

import static java.time.Duration.ofMinutes;

@Component
class IpRateLimiter implements RateLimiter<Command<?>> {

    ClientIp clientIp;

    IpRateLimiter(ClientIp clientIp) {
        this.clientIp = clientIp;
    }

    @Override
    public boolean matches(Command<?> command) {
        return clientIp.isAvailable();
    }

    @Override
    public Bandwidth bandwidth() {
        // Visitors can send multiple commands from a single IP, but not excessively.
        // After 60 requests per minute (rpm), command submissions will be temporarily blocked.
        // Access will resume at a rate of 10 rpm.
        var id = getClass().getSimpleName() + "/" + clientIp;
        return Bandwidth.builder().capacity(60).refillGreedy(10, ofMinutes(1)).id(id).build();
    }
}
