package cleanbank.domains.crm;

import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface Clients extends Repository<Client, UUID> {

  Client findById(UUID clientId);

  Optional<Client> findByNaturalId(String personalId);

  void add(Client client);

}
