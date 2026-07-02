plugins {
	java
	jacoco
	id("org.springframework.boot") version "4.1.0"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.flowpay"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Spring Boot 4 is modular: each capability is its own starter.
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// Database schema is versioned with Flyway. In Spring Boot 4 the Flyway
	// auto-configuration lives in its own module, so we depend on that (not just flyway-core)
	// for migrations to actually run on startup.
	implementation("org.springframework.boot:spring-boot-flyway")
	implementation("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")

	// OpenAPI docs + Swagger UI (springdoc 3.x targets Spring Boot 4).
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	// Spring Boot 4.1's BOM doesn't manage the Testcontainers modules, so pin them here.
	testImplementation(platform("org.testcontainers:testcontainers-bom:1.20.4"))
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// Test coverage report (JaCoCo). Running `./gradlew test` also produces it at
// build/reports/jacoco/test/html/index.html.
jacoco {
	toolVersion = "0.8.12"
}

tasks.test {
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		html.required.set(true)
		xml.required.set(true)
	}
	// The app bootstrap and framework config classes aren't the point of the report.
	classDirectories.setFrom(
		classDirectories.files.map {
			fileTree(it) {
				exclude(
					"**/RoutingMonitoringApplication.class",
					"**/infrastructure/config/**",
				)
			}
		}
	)
}
