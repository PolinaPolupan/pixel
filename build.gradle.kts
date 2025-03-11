import org.springframework.boot.gradle.tasks.run.BootRun
import org.gradle.api.DefaultTask


plugins {
	java
	id("org.springframework.boot") version "3.4.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example.pixel"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.jar {
	enabled = false
}

tasks.withType<Test> {
	useJUnitPlatform()
	dependsOn(":native:assemble")
	doFirst {
		// Get the actual path of the native build directory
		val nativeBuildDir = project(":native").layout.buildDirectory.get().asFile.absolutePath

		// Log the location and contents for debugging
		logger.lifecycle("Native build directory: $nativeBuildDir")
		logger.lifecycle("Directory exists: ${File(nativeBuildDir).exists()}")
		File(nativeBuildDir).listFiles()?.forEach { file ->
			logger.lifecycle("Found file: ${file.name}")
		}

		// Set the system property
		systemProperty("java.library.path", nativeBuildDir)

		// Also print the current java.library.path for debugging
		logger.lifecycle("Current java.library.path: ${System.getProperty("java.library.path")}")
	}
	systemProperty("java.library.path", project(":native").layout.buildDirectory.get().asFile.absolutePath)
}

tasks.named<BootRun>("bootTestRun") {
	dependsOn(":native:assemble")
	systemProperty("java.library.path", project(":native").layout.buildDirectory.get().asFile.absolutePath)
}

tasks.named<BootRun>("bootRun") {
	dependsOn(":native:assemble")
	systemProperty("java.library.path", project(":native").layout.buildDirectory.get().asFile.absolutePath)
}

tasks.named<DefaultTask>("build") {
	dependsOn(":native:assemble")
}

tasks.compileJava {
	options.compilerArgs = options.compilerArgs + listOf("-h", "${rootProject.projectDir}/native/src/include")
}
