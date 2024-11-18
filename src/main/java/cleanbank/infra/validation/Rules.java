package cleanbank.infra.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Rules {

  private final List<Rule> rules = new ArrayList<>();

  public <T> Rules define(Supplier<T> getter, Predicate<T> check, String message) {
    return define(getter, check, message, NestedRules.absent());
  }

  public <T> Rules define(Supplier<T> getter, Predicate<T> check, String message, NestedRules nested) {
    var rule = new AttributeRule<>(getter, check, message);
    rules.add(rule);
    rule.with(nested.rules());
    return this;
  }

  public void enforce() {
    var violations = violations();
    if (!violations.isEmpty()) {
      throw new Violations(violations);
    }
  }

  private List<String> violations() {
    return rules
      .stream()
      .flatMap(rule -> rule.violations().stream())
      .toList();
  }

  public interface NestedRules {

    static NestedRules absent() {
      return rules -> {
      };
    }

    void define(Rules rules);

    default Rules rules() {
      var rules = new Rules();
      define(rules);
      return rules;
    }
  }

  private interface Rule {
    List<String> violations();
  }

  private static class AttributeRule<V> implements Rule {
    private final Supplier<V> getter;
    private final Predicate<V> check;
    private final String message;
    private Rules nestedRules = new Rules();

    AttributeRule(Supplier<V> getter, Predicate<V> check, String message) {
      this.getter = getter;
      this.check = check;
      this.message = message;
    }

    void with(Rules rules) {
      this.nestedRules = rules;
    }

    @Override
    public List<String> violations() {
      var attr = this.getter.get();
      var truthy = this.check.test(attr);
      if (!truthy) {
        return List.of(message.formatted(attr));
      } else {
        return this.nestedRules.violations();
      }
    }
  }

  public static class Violations extends RuntimeException {

    private final List<String> violations;

    Violations(List<String> violations) {
      this.violations = violations;
    }

    public List<String> violations() {
      return violations;
    }

    @Override
    public String getMessage() {
      return String.join(", ", violations);
    }

  }
}
