package effective.bank.domain


import com.tngtech.archunit.core.importer.ClassFileImporter
import effective.bank.domain.model.DomainRepository
import org.springframework.data.repository.Repository
import spock.lang.Shared
import spock.lang.Specification

import javax.persistence.AttributeConverter
import javax.persistence.Converter
import javax.persistence.Embeddable
import javax.persistence.Entity
import javax.persistence.MappedSuperclass

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
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

    def "only repositories and domain entities can access Spring Data, JPA and Hibernate"() {
        given:
        final rule = noClasses()
                .that()
                .areNotAnnotatedWith(Converter)
                .and()
                .areNotAnnotatedWith(DomainRepository)
                .and()
                .areNotAnnotatedWith(Entity)
                .and()
                .areNotAnnotatedWith(Embeddable)
                .and()
                .areNotAnnotatedWith(MappedSuperclass)
                .and()
                .areNotAssignableTo(Specification)
                .should()
                .dependOnClassesThat().resideInAnyPackage("org.springframework.data..", "javax.persistence..", "org.hibernate..")
                .because("only repositories and entities should be dealing with storage")
        expect:
        rule.check(classes)
    }

    def "all repositories must be annotated with @DomainRepository"() {
        given:
        final rule = classes()
                .that()
                .areAssignableTo(Repository)
                .should()
                .beAnnotatedWith(DomainRepository)
                .because("we should be managing transactions at the service layer")
        expect:
        rule.check(classes)
    }

}