import com.adarshr.gradle.testlogger.theme.ThemeType

plugins {
  java
  groovy
  application
  id("com.adarshr.test-logger") version "4.0.0"
  id("org.springframework.boot") version "4.0.0"
  id("io.spring.dependency-management") version "1.1.7"
  id("com.github.ben-manes.versions") version "0.52.0"
}
repositories {
  mavenCentral()
}

dependencies {
  implementation("org.jspecify:jspecify:1.0.0")
  implementation("com.machinezoo.noexception:noexception:1.9.1")
  implementation("one.util:streamex:0.8.3")
  implementation("org.apache.commons:commons-lang3:3.18.0")
  implementation("org.springframework.retry:spring-retry:2.0.12")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-mail")
  implementation("org.springframework.boot:spring-boot-configuration-processor")
  implementation("com.github.f4b6a3:uuid-creator:6.1.0")
  implementation("com.github.ben-manes.caffeine:caffeine:3.2.0")
  implementation("com.bucket4j:bucket4j-core:8.10.1")
  implementation("com.google.guava:guava:33.4.8-jre")
  implementation("commons-validator:commons-validator:1.10.1") {
    exclude(module = "commons-logging")
  }

  testImplementation("com.h2database:h2:2.3.232")
  testImplementation("net.datafaker:datafaker:2.4.3")
  testImplementation("com.tngtech.archunit:archunit:1.4.1")
  testImplementation("org.springframework.boot:spring-boot-test")
  testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
  testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
  testImplementation("org.spockframework:spock-core:2.4-M6-groovy-4.0")
  testImplementation("org.spockframework:spock-spring:2.4-M6-groovy-4.0")
  testImplementation("org.apache.groovy:groovy:4.0.28")
  testImplementation("org.apache.groovy:groovy-json:4.0.28")
  testImplementation("com.google.guava:guava-testlib:33.4.8-jre")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

configurations.all {
  // Exclude useless Mockito dependencies added by spring-test-starters
  exclude(group = "org.mockito")
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
  testlogger {
    theme = ThemeType.MOCHA
    slowThreshold = 1000
  }
  testLogging {
    events("passed", "failed")
  }
}

