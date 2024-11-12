package cleanbank.infra.modeling;

public interface DomainEvent {

  interface Broadcast {
    void publish(DomainEvent event);
  }

}
