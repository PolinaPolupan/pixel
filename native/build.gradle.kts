val buildDir = layout.buildDirectory.asFile.get()

// Register a task to run CMake configuration
tasks.register<Exec>("configureCMake") {
    description = "Configure native build with CMake"
    group = "build"

    // Create build directory if it doesn't exist
    doFirst {
        buildDir.mkdirs()
    }

    workingDir(buildDir)
    commandLine("cmake", projectDir.absolutePath)
    logging.captureStandardOutput(LogLevel.QUIET)

    isIgnoreExitValue = false
}

// Register a task to run Make
tasks.register<Exec>("buildNative") {
    description = "Build native library using Make"
    group = "build"

    dependsOn("configureCMake")
    workingDir(buildDir)
    commandLine("make")
    logging.captureStandardOutput(LogLevel.INFO)

    isIgnoreExitValue = false
}

tasks.register<Delete>("clean") {
    delete(buildDir)
}

// Register a task for platform-independent builds
tasks.register<DefaultTask>("assemble") {
    description = "Assemble native library"
    group = "build"

    dependsOn("buildNative")

    doLast {
        logger.lifecycle("Native library built successfully at: ${layout.buildDirectory.asFile.get().absolutePath}")
    }
}