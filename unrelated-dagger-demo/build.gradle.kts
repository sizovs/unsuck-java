plugins {
    id(Plugins.commonConventions)
}

dependencies {
    annotationProcessor(Dependencies.daggerCompiler)
    testAnnotationProcessor(Dependencies.daggerCompiler)
    implementation(Dependencies.slf4jSimple)
    implementation(Dependencies.javalin)
    implementation(Dependencies.postgres)
    implementation(Dependencies.jooq)
    implementation(Dependencies.flyway)
    implementation(Dependencies.dagger)
    testImplementation(Dependencies.h2)
}



