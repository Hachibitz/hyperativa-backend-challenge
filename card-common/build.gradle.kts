plugins {
	`java-library`
	id("maven-publish")
}

group = "br.com.hyperativa"
version = "0.0.1-SNAPSHOT"
description = "Lib de classes compartilhadas entre api e consumer"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

repositories {
	mavenCentral()
}

val jacksonVersion = "2.20.0"
val springBootVersion = "3.5.6"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
	implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
	implementation("org.springframework.boot:spring-boot-starter-amqp:$springBootVersion")
	compileOnly("org.projectlombok:lombok:1.18.32")
	annotationProcessor("org.projectlombok:lombok:1.18.32")
	testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			groupId = project.group.toString()
			artifactId = "card-common"
			version = project.version.toString()

			from(components["java"])
		}
	}
	repositories {
		mavenLocal()
	}
}