package effective.bank.application.infra.scheduling;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
interface ScheduledCommandRepository extends Repository<ScheduledCommand, Long> {

    @Query(value = "SELECT * FROM scheduled_command cmd ORDER BY cmd.id ASC FOR UPDATE SKIP LOCKED LIMIT 1", nativeQuery = true)
    Optional<ScheduledCommand> findNextScheduledCommand();

    void save(ScheduledCommand command);

    void delete(ScheduledCommand command);
}