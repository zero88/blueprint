dependencies {
    api(project(":micro:micro-config"))
    api(project(":micro:micro-metadata"))
    api(project(":micro:rpc"))
    api(project(":http:client"))
    api(VertxLibs.serviceDiscovery)

    compileOnly(VertxLibs.codegen)
    compileOnly(VertxLibs.rx2)
    annotationProcessor(VertxLibs.codegen)

    testImplementation(VertxLibs.junit)
    testImplementation(testFixtures(project(":qwe-core")))
    testCompileOnly(VertxLibs.codegen)
}

tasks {
    register<JavaCodeGenTask>("annotationProcessing")
    compileJava {
        dependsOn(withType<JavaCodeGenTask>())
    }
}
