plugins {
	java
	id("org.springframework.boot") version "3.0.2"
	id("io.spring.dependency-management") version "1.1.0"
	id("com.google.cloud.tools.jib") version "3.3.1"
//	id("org.graalvm.buildtools.native") version "0.9.18"
}

group = "eu.thijslemmens.iot"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-integration")
	implementation("org.springframework.integration:spring-integration-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.integration:spring-integration-mqtt")
	implementation("org.eclipse.paho:org.eclipse.paho.mqttv5.client:1.2.5")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.integration:spring-integration-test")
	testImplementation("org.testcontainers:postgresql:1.17.6")
	testImplementation("org.testcontainers:testcontainers:1.17.6")
	testImplementation("org.testcontainers:junit-jupiter:1.17.6")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

val image: String? by project

jib {
	from {
		platforms {
			platform {
				architecture = "arm64"
				os = "linux"
			}
			platform {
				architecture = "amd64"
				os = "linux"
			}
		}
	}
}