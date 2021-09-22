object UtilLibs {

    object Version {

        const val lombok = "1.18.16"
        const val jetbrainsAnnotations = "20.1.0"
    }

    const val lombok = "org.projectlombok:lombok:${Version.lombok}"
    const val jetbrainsAnnotations = "org.jetbrains:annotations:${Version.jetbrainsAnnotations}"
}

object PluginLibs {

    object Version {

        const val jvm = "1.3.72"
        const val nexusPublish = "1.1.0"
        const val plugin = "2.0.0"
        const val jooq = "5.2"
    }

    const val jvm = "jvm"
    const val nexusPublish = "io.github.gradle-nexus.publish-plugin"
    const val oss = "io.github.zero88.gradle.oss"
    const val root = "io.github.zero88.gradle.root"
    const val app = "io.github.zero88.gradle.qwe.app"
    const val docker = "io.github.zero88.gradle.qwe.docker"
    const val jooq = "nu.studer.jooq"
}

object JSRLibs {

    object Version {

        const val annotation = "1.3.2"
        const val jaxrs = "2.1.1"
        const val persistence = "2.2"
    }

    const val annotation = "javax.annotation:javax.annotation-api:${Version.annotation}"
    const val jaxrs = "javax.ws.rs:javax.ws.rs-api:${Version.jaxrs}"
    const val persistence = "javax.persistence:javax.persistence-api:${Version.persistence}"

}

object JacksonLibs {

    object Version {

        const val jackson = "2.12.0"
    }

    const val core = "com.fasterxml.jackson.core:jackson-core:${Version.jackson}"
    const val annotations = "com.fasterxml.jackson.core:jackson-annotations:${Version.jackson}"
    const val databind = "com.fasterxml.jackson.core:jackson-databind:${Version.jackson}"
    const val datatype = "com.fasterxml.jackson.datatype:jackson-datatype-jdk8:${Version.jackson}"
    const val datetime = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Version.jackson}"
}

object TestLibs {

    object Version {

        const val junit5 = "5.7.0"
        const val jsonAssert = "1.5.0"
    }

    const val junit5Api = "org.junit.jupiter:junit-jupiter-api:${Version.junit5}"
    const val junit5Engine = "org.junit.jupiter:junit-jupiter-engine:${Version.junit5}"
    const val junit5Vintage = "org.junit.vintage:junit-vintage-engine:${Version.junit5}"
    const val jsonAssert = "org.skyscreamer:jsonassert:${Version.jsonAssert}"
}

object VertxLibs {

    object Version {

        const val vertx = "4.1.2"
        const val vertxAuth = "4.1.2"
    }

    const val core = "io.vertx:vertx-core:${Version.vertx}"
    const val codegen = "io.vertx:vertx-codegen:${Version.vertx}"
    const val rx2 = "io.vertx:vertx-rx-java2:${Version.vertx}"
    const val auth = "io.vertx:vertx-auth-common:${Version.vertxAuth}"
    const val jwt = "io.vertx:vertx-auth-jwt:${Version.vertxAuth}"
    const val ldap = "io.vertx:vertx-auth-ldap:${Version.vertx}"
    const val oauth2 = "io.vertx:vertx-auth-oauth2:${Version.vertx}"
    const val webauthn = "io.vertx:vertx-auth-webauthn:${Version.vertx}"
    const val shiro = "io.vertx:vertx-auth-shiro:${Version.vertx}"
    const val config = "io.vertx:vertx-config:${Version.vertx}"
    const val web = "io.vertx:vertx-web:${Version.vertx}"
    const val healthCheck = "io.vertx:vertx-health-check:${Version.vertx}"
    const val circuitBreaker = "io.vertx:vertx-circuit-breaker:${Version.vertx}"
    const val serviceDiscovery = "io.vertx:vertx-service-discovery:${Version.vertx}"
    const val hazelcast = "io.vertx:vertx-hazelcast:${Version.vertx}"
    const val zookeeper = "io.vertx:vertx-zookeeper:${Version.vertx}"
    const val ignite = "io.vertx:vertx-ignite:${Version.vertx}"
    const val infinispan = "io.vertx:vertx-infinispan:${Version.vertx}"
    const val junit = "io.vertx:vertx-unit:${Version.vertx}"
    const val junit5 = "io.vertx:vertx-junit5:${Version.vertx}"
    const val jdbc = "io.vertx:vertx-jdbc-client:${Version.vertx}"
    const val pgsql = "io.vertx:vertx-pg-client:${Version.vertx}"
    const val mysql = "io.vertx:vertx-mysql-client:${Version.vertx}"
    const val db2 = "io.vertx:vertx-db2-client:${Version.vertx}"
    const val mssql = "io.vertx:vertx-mssql-client:${Version.vertx}"

}

object LogLibs {

    object Version {

        const val slf4j = "1.7.30"
        const val logback = "1.2.3"
    }

    const val slf4j = "org.slf4j:slf4j-api:${Version.slf4j}"
    const val logback = "ch.qos.logback:logback-classic:${Version.logback}"
}

object ZeroLibs {
    object Version {

        const val jooqx = "1.1.0-SNAPSHOT"
        const val jpaExt = "0.9.0"
        const val rql = "0.9.0"
        const val utils = "2.0.0-SNAPSHOT"
    }

    const val utils = "io.github.zero88:java-utils:${Version.utils}"
    const val jpaExt = "io.github.zero88:jpa-ext:${Version.jpaExt}"
    const val jooqx = "io.github.zero88:jooqx-core:${Version.jooqx}"
    const val jooqxSpi = "io.github.zero88:jooqx-spi:${Version.jooqx}"
    const val jooqRql = "io.github.zero88:rql-jooq:${Version.rql}"
}

object DatabaseLibs {

    object Version {

        const val jooq = "3.14.8"
        const val h2 = "1.4.200"
        const val pgsql = "42.2.19"
        const val mysql = "8.0.23"
        const val hikari = "4.0.2"
        const val jpa = "2.2"
        const val jta = "1.3"
    }

    const val h2 = "com.h2database:h2:${Version.h2}"
    const val pgsql = "org.postgresql:postgresql:${Version.pgsql}"
    const val hikari = "com.zaxxer:HikariCP:${Version.hikari}"
    const val jooq = "org.jooq:jooq:${Version.jooq}"
    const val jooqMeta = "org.jooq:jooq-meta:${Version.jooq}"
    const val jooqMetaExt = "org.jooq:jooq-meta-extensions:${Version.jooq}"
    const val jooqCodegen = "org.jooq:jooq-codegen:${Version.jooq}"
}

object TestContainers {
    object Version {

        const val ver = "1.15.2"
    }

    const val junit5 = "org.testcontainers:junit-jupiter:${Version.ver}"
    const val pgsql = "org.testcontainers:postgresql:${Version.ver}"
}
