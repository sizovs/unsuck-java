package effective.bank.application.infra.pipeline.middlewares.resilience;

class RateLimitException extends RuntimeException {
  RateLimitException() {
    super("Rate limit has been reached");
  }
}