package effective.bank.application.infra.scheduling;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Pipeline;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class Scheduler {

    private final ScheduledCommandRepository repository;
    private final Pipeline pipeline;

    public Scheduler(ScheduledCommandRepository repository, Pipeline pipeline) {
        this.repository = repository;
        this.pipeline = pipeline;
    }

    public void schedule(Command command) {
        var scheduledCommand = new ScheduledCommand(command);
        repository.save(scheduledCommand);
    }

    @Transactional
    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    public void executeLatestRegularly() {
        var command = repository.findNextScheduledCommand();
        command.ifPresent(cmd -> {
            cmd.execute(pipeline);
            repository.delete(cmd);
        });
    }
}