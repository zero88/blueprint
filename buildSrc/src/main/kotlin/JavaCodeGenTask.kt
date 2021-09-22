import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.the

open class JavaCodeGenTask : JavaCompile() {

    @Input
    val sourceName = project.objects.property<String>().convention("main")

    init {
        group = "other"
        val sources = project.the<JavaPluginConvention>().sourceSets.getByName(sourceName.get())
        source = sources.java
        destinationDir = project.file("${project.buildDir}/generated/${sourceName.get()}/java")
        classpath = sources.compileClasspath
        options.annotationProcessorPath = classpath
        options.compilerArgs = listOf(
            "-proc:only",
            "-processor", "io.vertx.codegen.CodeGenProcessor",
            "-Acodegen.output=${project.projectDir}/src/${sourceName.get()}"
        )
        sources.java.srcDirs(destinationDir)
    }
}
