package effective.bank.domain.model;

import com.google.common.collect.ImmutableList;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Collection;

@MappedSuperclass
public abstract class DomainEntity<T> {

    @Transient
    private final transient Collection<DomainEvent> publishedEvents = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public boolean satisfies(Specification<T> specification) {
        return specification.isSatisfiedBy((T) this);
    }

    protected void publish(DomainEvent event) {
        var eventPublisher = DomainEventPublisher.getInstance();
        eventPublisher.publish(event);
        publishedEvents.add(event);
    }

    protected Collection<DomainEvent> publishedEvents() {
        return ImmutableList.copyOf(publishedEvents);
    }

}