package cleanbank.domains.crm;

import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public interface Clients extends Repository<Client, UUID> {

  Client findById(UUID clientId);

  Optional<Client> findByNaturalId(String personalId);

  void add(Client client);

}
