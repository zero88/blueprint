plugins {
    id(PluginLibs.oss) version PluginLibs.Version.plugin
    id(PluginLibs.root) version PluginLibs.Version.plugin apply false
    id(PluginLibs.jooq) version PluginLibs.Version.jooq apply false
    id(PluginLibs.nexusPublish) version PluginLibs.Version.nexusPublish
}

allprojects {
    group = "io.github.zero88.qwe"

    repositories {
        mavenLocal()
        maven { url = uri("https://maven-central-asia.storage-download.googleapis.com/maven2/") }
        jcenter()
        maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
        mavenCentral()
    }

    apply(plugin = PluginLibs.oss)

    oss {
        zero88.set(true)
        publishingInfo {
            homepage.set("https://github.com/zero88/qwe")
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://github.com/zero88/qwe/blob/master/LICENSE")
            }
            scm {
                connection.set("scm:git:git://git@github.com:zero88/qwe.git")
                developerConnection.set("scm:git:ssh://git@github.com:zero88/qwe.git")
                url.set("https://github.com/zero88/qwe")
            }
        }
    }
}

subprojects {
    apply(plugin = "eclipse")
    apply(plugin = "idea")

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    dependencies {
        compileOnly(UtilLibs.lombok)
        annotationProcessor(UtilLibs.lombok)

        testImplementation(TestLibs.junit5Api)
        testImplementation(TestLibs.junit5Engine)
        testImplementation(TestLibs.junit5Vintage)
        testImplementation(TestLibs.jsonAssert)
        testCompileOnly(UtilLibs.lombok)
        testAnnotationProcessor(UtilLibs.lombok)
    }
}

apply(plugin = PluginLibs.root)

nexusPublishing {
    packageGroup.set("io.github.zero88")
    repositories {
        sonatype {
            username.set(project.property("nexus.username") as String?)
            password.set(project.property("nexus.password") as String?)
        }
    }
}

tasks.register("generateJooq") {
    group = "jooq"
    dependsOn(subprojects.map { it.tasks.withType<nu.studer.gradle.jooq.JooqGenerate>() })
}
