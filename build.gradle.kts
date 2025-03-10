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
	val nativeDir = file("${projectDir}/native/build") // Turnaround for the wsl serialization path problem
	systemProperty("java.library.path", nativeDir.absolutePath)
}

tasks.named<BootRun>("bootTestRun") {
	dependsOn(":native:assemble")
	val nativeDir = file("${projectDir}/native/build") // Turnaround for the wsl serialization path problem
	systemProperty("java.library.path", nativeDir.absolutePath)
}

tasks.named<BootRun>("bootRun") {
	dependsOn(":native:assemble")
	val nativeDir = file("${projectDir}/native/build") // Turnaround for the wsl serialization path problem
	systemProperty("java.library.path", nativeDir.absolutePath)
}

tasks.named<DefaultTask>("build") {
	dependsOn(":native:assemble")
}

tasks.compileJava {
	options.compilerArgs = options.compilerArgs + listOf("-h", "${rootProject.projectDir}/native/src/include")
}
