import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.0.6"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.7.22"
	kotlin("plugin.spring") version "1.7.22"
}

group = "com.isel"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// Kafka
	implementation("org.apache.kafka:kafka-clients:3.4.0")
	implementation("org.apache.kafka:kafka-streams:3.4.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

task<Copy>("extractUberJar") {
	dependsOn("assemble")
	// opens the JAR containing everything...
	from(zipTree("$buildDir/libs/${rootProject.name}-$version.jar"))
	// ... into the 'build/dependency' folder
	into("build/dependency")
}

tasks.named("build") {
	dependsOn("assemble", "extractUberJar")
}
