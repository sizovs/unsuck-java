import io.spring.gradle.dependencymanagement.DependencyManagementPlugin
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id(Plugins.springBoot) version PluginVersions.SPRING_BOOT
    id(Plugins.commonConventions)
}

apply<DependencyManagementPlugin>()

dependencies {
    implementation(projects.utils)
    implementation(Dependencies.javaxJson)
    implementation(Dependencies.springBootJpa)
    implementation(Dependencies.springBootTest)
    implementation(Dependencies.commonsLang)
    implementation(Dependencies.commonsValidator)
    implementation(Dependencies.protonPack)
    implementation(Dependencies.threeTenExtra)
    implementation(Dependencies.streamEx)
    testImplementation(Dependencies.spockSpring)
    testImplementation(Dependencies.h2)
}

tasks.withType<BootJar> {
    enabled = false
}

