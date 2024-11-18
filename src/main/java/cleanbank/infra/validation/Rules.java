package cleanbank.infra.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;

public class Rules<R> {

  private final List<Rule<R>> rules = new ArrayList<>();

  public <T> Rules<R> define(Supplier<T> getter, Predicate<T> check, String message) {
    return define(getter, check, message, NestedRules.absent());
  }

  public <T> Rules<R> define(Supplier<T> getter, Predicate<T> check, String message, NestedRules<R> nested) {
    var rule = new AttributeRule<>(getter, check, message);
    rules.add(rule);
    rule.with(nested.rules());
    return this;
  }

  public void enforce() {
    var violations = check();
    if (!violations.isEmpty()) {
      throw new Violations(violations);
    }
  }

  private List<String> check() {
    return rules
      .stream()
      .flatMap(rule -> rule.violations().stream())
      .toList();
  }

  public interface NestedRules<R> {

    static <T> NestedRules<T> absent() {
      return rules -> {
      };
    }

    void define(Rules<R> rules);

    default Rules<R> rules() {
      var rules = new Rules<R>();
      define(rules);
      return rules;
    }
  }

  private interface Rule<R> {
    Collection<String> violations();
  }

  private class AttributeRule<V> implements Rule<R> {
    private final Supplier<V> getter;
    private final Predicate<V> check;
    private final String message;
    private Rules<R> nestedRules = new Rules<>();

    AttributeRule(Supplier<V> getter, Predicate<V> check, String message) {
      this.getter = getter;
      this.check = check;
      this.message = message;
    }

    void with(Rules<R> rules) {
      this.nestedRules = rules;
    }

    @Override
    public Collection<String> violations() {
      var attr = this.getter.get();
      var truthy = this.check.test(attr);
      if (!truthy) {
        return singletonList(message.formatted(attr));
      } else {
        return this.nestedRules.check();
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
