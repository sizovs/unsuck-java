package cleanbank.infra.pipeline;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
interface Jobs extends Repository<Job, Long> {

  @Query(value = "SELECT * FROM job j ORDER BY j.id ASC LIMIT 1 FOR UPDATE SKIP LOCKED", nativeQuery = true)
  Optional<Job> next();

  void add(Job job);

  void delete(Job job);
}
