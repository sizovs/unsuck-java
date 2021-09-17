plugins {
    java
    groovy
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://artifactory.cronapp.io/public-release/")
}



dependencies {
    implementation(Dependencies.slf4jApi)
    implementation(Dependencies.guava)
    testImplementation(Dependencies.groovy)
    testImplementation(Dependencies.groovyJson)
    testImplementation(Dependencies.spock)
    testImplementation(Dependencies.junitEngine)
    testImplementation(Dependencies.junitParams)
    testImplementation(Dependencies.junitApi)
    testImplementation(Dependencies.guavaTestlib)
    testImplementation(Dependencies.faker)
    testImplementation(Dependencies.jsonAssert)
    testImplementation(Dependencies.archUnit)
}

tasks.test {
    maxParallelForks = 4
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}


