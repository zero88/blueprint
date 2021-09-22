dependencies {
    api(project(":qwe-core"))
    api(VertxLibs.web)
    api(JSRLibs.jaxrs)
    
    compileOnly(project(":micro:discovery"))
    compileOnly(VertxLibs.serviceDiscovery)

    testImplementation(VertxLibs.junit)
    testImplementation(testFixtures(project(":qwe-core")))
    testImplementation(project(":micro:discovery"))
    testImplementation(project(":http:client"))
}
