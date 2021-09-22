plugins {
    `java-test-fixtures`
}

dependencies {
    api(JacksonLibs.core)
    api(JacksonLibs.databind)
    api(JacksonLibs.datetime)
    api(VertxLibs.core)
    api(VertxLibs.auth)
    api(ZeroLibs.utils)
    api(ZeroLibs.jpaExt)
    api(JSRLibs.annotation)
    api(LogLibs.slf4j)
    api(UtilLibs.jetbrainsAnnotations)

    compileOnly(VertxLibs.hazelcast)
    compileOnly(VertxLibs.zookeeper)
    compileOnly(VertxLibs.ignite)
    compileOnly(VertxLibs.infinispan)

    compileOnly(VertxLibs.codegen)
    compileOnly(VertxLibs.rx2)
    compileOnlyApi(JacksonLibs.annotations)
    annotationProcessor(VertxLibs.codegen)

    testImplementation(VertxLibs.junit)
    testImplementation(VertxLibs.junit5)
    testImplementation(VertxLibs.rx2)
    testImplementation(VertxLibs.jwt)

    testFixturesApi(testFixtures(ZeroLibs.utils))
    testFixturesApi(LogLibs.logback)
    testFixturesApi(TestLibs.junit5Api)
    testFixturesApi(TestLibs.junit5Engine)
    testFixturesApi(TestLibs.junit5Vintage)
    testFixturesApi(TestLibs.jsonAssert)
    testFixturesApi(VertxLibs.junit)
    testFixturesApi(VertxLibs.junit5)
    testFixturesCompileOnly(VertxLibs.rx2)
    testFixturesCompileOnly(UtilLibs.lombok)
    testFixturesAnnotationProcessor(UtilLibs.lombok)
}

tasks {
    register<JavaCodeGenTask>("annotationProcessing")
    compileJava {
        dependsOn(withType<JavaCodeGenTask>())
    }
}
