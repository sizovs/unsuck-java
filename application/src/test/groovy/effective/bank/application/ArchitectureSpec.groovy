package effective.bank.application

import an.awesome.pipelinr.Command
import com.tngtech.archunit.core.importer.ClassFileImporter
import effective.bank.application.infra.spring.annotations.PrototypeComponent
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared
import spock.lang.Specification

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices

class ArchitectureSpec extends Specification {

    @Shared
    def classes = new ClassFileImporter().importClasspath()

    def "no package cycles allowed"() {
        given:
        final rule = slices().matching("..(*)..").should().beFreeOfCycles()
        expect:
        rule.check(classes)
    }

    def "all handlers must have a prototype scope"() {
        given:
        final rule = classes()
                .that()
                .areAssignableTo(Command.Handler)
                .should()
                .beAnnotatedWith(PrototypeComponent)
                .because("we want every handler to have a prototype scope")
        expect:
        rule.check(classes)
    }

    def "commands cannot depend on domain model"() {
        given:
        final rule = noClasses()
                .that()
                .areAssignableTo(Command)
                .should()
                .dependOnClassesThat().resideInAPackage("*..domain..*")
                .because("command handlers deal with domain entities")
        expect:
        rule.check(classes)
    }

    def "no @Autowired annotation allowed"() {
        given:
        final rule = noCodeUnits()
                .should()
                .beAnnotatedWith(Autowired)
                .because("Spring uses constructor injection by default and such annotation are redundant")
        expect:
        rule.check(classes)
    }

}