package cleanbank

import cleanbank.infra.modeling.VisibleForHibernate
import cleanbank.infra.pipeline.Command
import cleanbank.infra.spring.annotations.PrototypeScoped
import com.google.common.annotations.VisibleForTesting
import com.tngtech.archunit.core.domain.JavaAccess
import com.tngtech.archunit.core.domain.JavaConstructor
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared
import spock.lang.Specification

import static com.tngtech.archunit.lang.SimpleConditionEvent.violated
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices

class ArchitectureSpec extends Specification {

  final static ROOT_PACKAGE = "cleanbank"

  @Shared
  def importedClasses = new ClassFileImporter().importPackages(ROOT_PACKAGE)

  def "no package cycles allowed"() {
    given:
    final rule = slices().matching("..(*)..").should().beFreeOfCycles()
    expect:
    rule.check(importedClasses)
  }

  def "all reactions must have prototype scope"() {
    given:
    final rule = classes()
      .that()
      .areAssignableTo(Command.Reaction)
      .and()
      .areNotInterfaces()
      .should()
      .beAnnotatedWith(PrototypeScoped)
      .because("we want every reaction to be stateful")
    expect:
    rule.check(importedClasses)
  }

  def "infra cannot depend on domain model and commands"() {
    given:
    final rule = noClasses()
      .that()
      .resideInAPackage("..infra..")
      .should()
      .dependOnClassesThat()
      .resideInAnyPackage("..domains..", "..commands..")
    expect:
    rule.check(importedClasses)
  }

  def "constructors marked with @VisibleForHibernate cannot be called from the outside"() {
    given:
    final rule = constructors()
      .that()
      .areAnnotatedWith(VisibleForHibernate)
      .should(onlyBeCalledBySameClassMembers())
    expect:
    rule.check(importedClasses)
  }

  def "methods marked with @VisibleForTesting can only be called by tests"() {
    given:
    final rule = codeUnits()
      .that()
      .areAnnotatedWith(VisibleForTesting)
      .should()
      .onlyBeCalled()
      .byClassesThat()
      .areAssignableTo(Specification)
    expect:
    rule.check(importedClasses)
  }

  def "commands cannot depend on domain model"() {
    given:
    final rule = noClasses()
      .that()
      .areAssignableTo(Command)
      .should()
      .dependOnClassesThat().resideInAPackage("..domains..")
      .because("only reactions deal with domain entities")
    expect:
    rule.check(importedClasses)
  }

  def "no @Autowired annotation allowed"() {
    given:
    final rule = noConstructors()
      .should()
      .beAnnotatedWith(Autowired)
      .because("Spring uses constructor injection and $Autowired.name is redundant")
    expect:
    rule.check(importedClasses)
  }

  private static ArchCondition<JavaConstructor> onlyBeCalledBySameClassMembers() {
    return new ArchCondition<JavaConstructor>("only be called by members of the same class") {
      @Override
      void check(JavaConstructor targetConstructor, ConditionEvents events) {
        targetConstructor.accessesToSelf.each { JavaAccess call ->
          def callerClass = call.originOwner
          def targetClass = targetConstructor.owner
          def isSameClass = callerClass == targetClass

          if (!isSameClass) {
            def message = "Constructor ${targetConstructor.fullName} in ${targetClass.name} should only be called by members in the same class, but was called by ${call.origin.fullName}"
            events.add(violated(targetConstructor, message))
          }
        }
      }
    }
  }
}
