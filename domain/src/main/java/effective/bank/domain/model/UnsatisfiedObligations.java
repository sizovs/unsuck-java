package effective.bank.domain.model;

interface UnsatisfiedObligations {

  UnsatisfiedObligations NONE = () -> false;

  boolean exist();
}