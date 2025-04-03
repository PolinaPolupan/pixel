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
	implementation("com.google.guava:guava:33.4.0-jre")
	implementation("org.reflections:reflections:0.10.2")
	implementation(platform("software.amazon.awssdk:bom:2.31.10"))
	implementation("software.amazon.awssdk:s3")
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
