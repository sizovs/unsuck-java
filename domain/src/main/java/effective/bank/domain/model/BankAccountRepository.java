package effective.bank.domain.model;

import org.springframework.data.repository.Repository;

@DomainRepository
public interface BankAccountRepository extends Repository<BankAccount, String> {
    void save(BankAccount account);

    BankAccount getOne(String iban);

    boolean existsById(String iban);

}
