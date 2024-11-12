package cleanbank.domains.accounts;

import java.util.UUID;

interface UnsatisfiedObligations {

  UnsatisfiedObligations NONE = (UUID clientId) -> false;

  boolean exist(UUID clientId);
}
