package effective.bank.domain.model;

@FunctionalInterface
public interface Specification<T> {
  boolean isSatisfiedBy(T entity);
}