package cleanbank.infra.modeling;

import cleanbank.infra.modeling.DomainEvent.Broadcast;
import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.*;

@MappedSuperclass
public abstract class DomainEntity<T> {

  @Id
  private UUID id = UlidCreator.getUlid().toUuid();

  @Version
  private long version;

  @Transient
  public final transient Collection<DomainEvent> publishedEvents = new ArrayList<>();

  protected void publish(DomainEvent event) {
    Spring.BROADCAST.ifPresent(broadcast -> broadcast.publish(event));
    publishedEvents.add(event);
  }

  @Component
  private static class Spring {
    private static Optional<Broadcast> BROADCAST = Optional.empty();

    Spring(ApplicationEventPublisher publisher) {
      BROADCAST = Optional.of(publisher::publishEvent);
    }
  }

  @SuppressWarnings("unchecked")
  public boolean $(Specification<T> specification) {
    return specification.isSatisfiedBy((T) this);
  }

  public UUID id() {
    return id;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other instanceof DomainEntity<?> that) {
      return Objects.equals(this.id(), that.id());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id());
  }

}
