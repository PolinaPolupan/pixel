plugins {
    java
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Use JUnit test framework
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("com.example.mypixel.processor.App")
}

tasks.named<Test>("test") {
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
    }
    dependsOn(":image-processor:native:assemble")
    systemProperty("java.library.path", project(":image-processor:native").layout.buildDirectory.get())
}

tasks.named<JavaExec>("run") {
    dependsOn(":image-processor:native:assemble")
    systemProperty("java.library.path", project(":image-processor:native").layout.buildDirectory.get())
}

tasks.compileJava {
    options.compilerArgs = options.compilerArgs + listOf("-h", "${rootProject.projectDir}/native/src/include")
}

tasks.jar {
    manifest {
        attributes(
            mapOf("Main-Class" to "com.example.mypixel.processor.App")
        )
    }
}