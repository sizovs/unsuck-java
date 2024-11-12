package cleanbank.infra.pipeline;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
class Scheduler implements Later {

  private final ScheduledCommands scheduledCommands;

  Scheduler(ScheduledCommands scheduledCommands) {
    this.scheduledCommands = scheduledCommands;
  }

  public <C extends Command<?>> void enqueue(C command) {
    var scheduledCommand = new ScheduledCommand(command);
    scheduledCommands.add(scheduledCommand);
  }

  @Transactional
  @Scheduled(initialDelay = 5000, fixedDelay = 5000)
  public void runPeriodically() {
    scheduledCommands.next().ifPresent(cmd -> {
      cmd.now();
      scheduledCommands.delete(cmd);
    });
  }
}

@Component
interface ScheduledCommands extends Repository<ScheduledCommand, Long> {

  @Query(value = "SELECT * FROM scheduled_command cmd ORDER BY cmd.id ASC LIMIT 1 FOR UPDATE SKIP LOCKED", nativeQuery = true)
  Optional<ScheduledCommand> next();

  void add(ScheduledCommand command);

  void delete(ScheduledCommand command);
}
