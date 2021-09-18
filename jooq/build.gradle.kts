import org.flywaydb.core.Flyway
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.*
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Logging.TRACE
import org.jooq.meta.jaxb.Target
import org.testcontainers.containers.PostgreSQLContainer

plugins {
    id(Plugins.commonConventions)
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(Dependencies.jooq)
        classpath(Dependencies.jooqMeta)
        classpath(Dependencies.jooqCodegen)
        classpath(Dependencies.flyway)
        classpath(Dependencies.testContainersPostgres)
        classpath(Dependencies.postgres)
    }
}


val jooqOutput = "$buildDir/generated-src/jooq"

sourceSets {
    main {
        java {
            srcDirs(jooqOutput)
        }
    }
}


tasks.register("generateJooq") {
    doLast {
        PostgreSQLContainer<Nothing>("postgres:11.5").use { pg ->
            pg.start()

            Flyway.configure()
                .locations("filesystem:$projectDir/src/main/resources/db/migration")
                .dataSource(pg.jdbcUrl, pg.username, pg.password)
                .load()
                .migrate()

            val configuration = Configuration()
                .withLogging(TRACE)
                .withJdbc(
                    Jdbc()
                        .withDriver("org.postgresql.Driver")
                        .withUrl(pg.jdbcUrl)
                        .withUser(pg.username)
                        .withPassword(pg.password)
                )
                .withGenerator(
                    Generator()
                        .withGenerate(
                            Generate()
                                .withJavaTimeTypes(true)
                                .withFluentSetters(true)
                        )
                        .withDatabase(
                            Database()
                                .withName("org.jooq.meta.postgres.PostgresDatabase")
                                .withIncludes(".*")
                                .withExcludes("")
                                .withInputSchema("public")
                                .withRecordVersionFields("VERSION")
                        )
                        .withTarget(
                            Target()
                                .withClean(true)
                                .withDirectory(jooqOutput)
                                .withPackageName("jooq")
                        )
                )

            GenerationTool.generate(configuration)
        }

    }
}


dependencies {
    implementation(Dependencies.jooq)
}