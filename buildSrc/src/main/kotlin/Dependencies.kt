object Versions {
    const val groovy = "3.0.9"
    const val junit = "5.5.1"
    const val guava = "29.0-jre"
    const val jackson = "2.12.5"
    const val jooq = "3.12.1"
    const val dagger = "2.29.1"
    const val slf4j = "1.7.32"
}


object Dependencies {
    const val jooq = "org.jooq:jooq:${Versions.jooq}"
    const val jooqMeta = "org.jooq:jooq-meta:${Versions.jooq}"
    const val jooqCodegen = "org.jooq:jooq-codegen:${Versions.jooq}"
    const val flyway = "org.flywaydb:flyway-core:6.0.7"


    const val javalin = "io.javalin:javalin:4.0.0"
    const val dagger = "com.google.dagger:dagger:${Versions.dagger}"
    const val daggerCompiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"

    const val junitEngine = "org.junit.jupiter:junit-jupiter-engine:${Versions.junit}"
    const val junitParams = "org.junit.jupiter:junit-jupiter-params:${Versions.junit}"
    const val junitApi = "org.junit.jupiter:junit-jupiter-api:${Versions.junit}"
    const val archUnit = "com.tngtech.archunit:archunit-junit5-engine:0.21.0"
    const val testContainersPostgres = "org.testcontainers:postgresql:1.15.0"
    const val h2 = "com.h2database:h2:1.4.200"

    const val postgres = "org.postgresql:postgresql:42.2.14"

    const val springBootJpa = "org.springframework.boot:spring-boot-starter-data-jpa"
    const val springBootTest = "org.springframework.boot:spring-boot-starter-test"


    const val groovy = "org.codehaus.groovy:groovy:${Versions.groovy}"
    const val groovyJson = "org.codehaus.groovy:groovy-json:${Versions.groovy}"

    const val pipelinr = "an.awesome:pipelinr:0.5"
    const val bucket4j = "com.github.vladimir-bukhtoyarov:bucket4j-core:4.10.0"
    const val jasypt = "org.jasypt:jasypt:1.9.3"
    const val threeTenExtra = "org.threeten:threeten-extra:1.5.0"
    const val protonPack = "com.codepoetics:protonpack:1.16"
    const val commonsValidator = "commons-validator:commons-validator:1.6"
    const val commonsLang = "org.apache.commons:commons-lang3:3.9"
    const val guava = "com.google.guava:guava:${Versions.guava}"
    const val guavaTestlib = "com.google.guava:guava-testlib:${Versions.guava}"
    const val ulid = "de.huxhorn.sulky:de.huxhorn.sulky.ulid:8.2.0"
    const val streamEx = "one.util:streamex:0.7.0"
    const val noException = "com.machinezoo.noexception:noexception:1.6.2"

    const val faker = "com.github.javafaker:javafaker:1.0.0"
    const val spock = "org.spockframework:spock-core:2.0-groovy-3.0"
    const val spockSpring = "org.spockframework:spock-spring:2.0-groovy-3.0"
    const val slf4jApi = "org.slf4j:slf4j-api:${Versions.slf4j}"
    const val slf4jSimple = "org.slf4j:slf4j-simple:${Versions.slf4j}"
    const val jsonAssert = "org.skyscreamer:jsonassert:1.5.0"
    const val javaxJson = "org.glassfish:javax.json:1.1.4"
    const val jackson = "com.fasterxml.jackson.core:jackson-databind:${Versions.jackson}"
    const val jacksonDateTime = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Versions.jackson}"

}


