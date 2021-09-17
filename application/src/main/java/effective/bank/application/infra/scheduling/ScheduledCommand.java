package effective.bank.application.infra.scheduling;

import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Pipeline;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
class ScheduledCommand {

  @Id
  @GeneratedValue
  private Long id;

  @Convert(converter = CommandJpaConverter.class)
  private Command command;

  ScheduledCommand(Command command) {
    this.command = command;
  }

  private ScheduledCommand() {}

  void execute(Pipeline pipeline) {
    command.execute(pipeline);
  }
}