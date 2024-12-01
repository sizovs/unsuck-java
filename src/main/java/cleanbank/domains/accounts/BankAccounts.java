package cleanbank.domains.accounts;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface BankAccounts extends Repository<BankAccount, UUID> {

  @Query("select it from BankAccount it where it.iban.iban = ?1")
  BankAccount findByIban(String iban);

  @Query("select count(it) = 0 from BankAccount it where it.iban.iban = ?1")
  boolean notExistsByIban(String iban);

  void add(BankAccount account);

}
