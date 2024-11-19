package cleanbank.infra.pipeline;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkArgument;

public class RateLimited implements Now {

  private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
    .expireAfterAccess(Duration.ofMinutes(15))
    .build();

  private final ObjectProvider<RateLimiter<Command<?>>> rateLimiters;

  private final Now origin;

  RateLimited(ObjectProvider<RateLimiter<Command<?>>> rateLimiters, Now origin) {
    this.rateLimiters = rateLimiters;
    this.origin = origin;
  }

  @Override
  public <C extends Command<R>, R> R execute(C command) {
    var rateLimiter = rateLimiters
      .stream()
      .filter(it -> it.matches(command))
      .findFirst();

    rateLimiter
      .map(this::bucket)
      .ifPresent(bucket -> {
        if (!bucket.tryConsume(1)) {
          throw new TooManyRequests();
        }
      });

    return origin.execute(command);
  }

  private Bucket bucket(RateLimiter<Command<?>> rateLimiter) {
    var bandwidth = rateLimiter.bandwidth();
    checkArgument(bandwidth.hasId(), "Bandwidth must have ID");
    return buckets.get(bandwidth.getId(), id -> Bucket.builder().addLimit(bandwidth).build());
  }

  public static class TooManyRequests extends RuntimeException {
    TooManyRequests() {
      super("Rate limit has been reached");
    }
  }
}
