package cleanbank.infra.modeling;

public interface Specification<T> {
  boolean isSatisfiedBy(T entity);
}