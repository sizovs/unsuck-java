package effective.bank.domain.model;

import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Optional;

public interface DomainEventPublisher {

    void publish(DomainEvent event);

    static DomainEventPublisher getInstance() {
        return SpringManaged.INSTANCE.orElse(Empty.INSTANCE);
    }

    @Component
    class SpringManaged implements DomainEventPublisher {
        private static Optional<DomainEventPublisher> INSTANCE = Optional.empty();
        private final ApplicationEventPublisher applicationEventPublisher;

        SpringManaged(ApplicationEventPublisher applicationEventPublisher) {
            this.applicationEventPublisher = applicationEventPublisher;
            INSTANCE = Optional.of(this);
        }

        @Override
        public void publish(DomainEvent event) {
            applicationEventPublisher.publishEvent(event);
        }
    }

    class Empty implements DomainEventPublisher {
        private static final DomainEventPublisher INSTANCE = new Empty();

        @Override
        public void publish(DomainEvent event) {
            var logger = LoggerFactory.getLogger(Empty.class);
            logger.warn("No {} configured. Cannot send event {}", DomainEventPublisher.class.getSimpleName(), event);
        }
    }
}