package effective.bank.application.infra.pipeline.middlewares.resilience;

import io.github.bucket4j.Bandwidth;

public interface RateLimited {

    Bandwidth bandwidth();

}