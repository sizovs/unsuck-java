package cleanbank.infra.pipeline;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class TransactionalOutbox implements Later {

  private final Jobs jobs;

  TransactionalOutbox(Jobs jobs) {
    this.jobs = jobs;
  }

  public <C extends Command<?>> void enqueue(C command) {
    var job = new Job(command);
    jobs.add(job);
  }

  @Transactional
  @Scheduled(fixedRate = 500)
  public void runPeriodically() {
    jobs.next().ifPresent(job -> {
      job.execute();
      jobs.delete(job);
    });
  }

}
