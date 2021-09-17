package effective.bank.application.infra.pipeline.middlewares.resilience;

import an.awesome.pipelinr.Command;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(10)
class RateLimitingMiddleware implements Command.Middleware {

    private final ConcurrentHashMap<Type, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public <R, C extends Command<R>> R invoke(C command, Next<R> next) {
        if (command instanceof RateLimited rateLimited) {
            var bandwidth = rateLimited.bandwidth();
            var bucket = bucket(command, bandwidth);
            if (bucket.tryConsume(1)) {
                return next.invoke();
            } else {
                throw new RateLimitException();
            }
        } else {
            return next.invoke();
        }
    }

    private <R, C extends Command<R>> Bucket bucket(C command, Bandwidth bandwidth) {
        var commandClass = command.getClass();
        return buckets.computeIfAbsent(
                commandClass, type -> Bucket4j.builder().addLimit(bandwidth).build());
    }
}