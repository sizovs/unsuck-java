package cleanbank.infra.jpa;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

public class BaseJpaRepository<T, ID> extends SimpleJpaRepository<T, ID> {

  private final EntityManager em;

  BaseJpaRepository(JpaEntityInformation<T, ID> entityInfo, EntityManager em) {
    super(entityInfo, em);
    this.em = em;
  }

  @NonNull
  @Deprecated
  public <S extends T> S save(@NonNull S entity) {
    throw new UnsupportedOperationException("save() method is not supported. Use add() instead");
  }

  public <S extends T> void add(@NonNull S entity) {
    em.persist(entity);
  }

  public <NID> Optional<T> findByNaturalId(@NonNull NID naturalId) {
    return em
      .unwrap(Session.class)
      .bySimpleNaturalId(this.getDomainClass())
      .loadOptional(naturalId);
  }
}
