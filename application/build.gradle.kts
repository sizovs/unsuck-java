import io.spring.gradle.dependencymanagement.DependencyManagementPlugin
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    application
    id(Plugins.commonConventions)
    id(Plugins.springBoot) version PluginVersions.SPRING_BOOT
}

apply<DependencyManagementPlugin>()

dependencies {

    implementation(projects.domain)
    implementation(projects.utils)

    implementation(Dependencies.postgres)
    implementation(Dependencies.noException)
    implementation(Dependencies.jasypt)
    implementation(Dependencies.bucket4j)
    implementation(Dependencies.pipelinr)
    implementation(Dependencies.jackson)
    implementation(Dependencies.jacksonDateTime)
    implementation(Dependencies.springBootJpa)
    testImplementation(Dependencies.testContainersPostgres)
}

tasks.withType<BootRun> {
    classpath = sourceSets["test"].runtimeClasspath
}

application {
    mainClass.set("effective.bank.EffectiveBank")
}






