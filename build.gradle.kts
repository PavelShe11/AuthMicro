import com.google.protobuf.gradle.id

plugins {
    java
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.protobuf") version "0.9.4"
}

group = "io.github.pavelshe11"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
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

extra["springGrpcVersion"] = "0.9.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("io.grpc:grpc-services")
    implementation("net.devh:grpc-client-spring-boot-starter:2.15.0.RELEASE")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
//    implementation("net.devh:grpc-server-spring-boot-starter:2.15.0.RELEASE")

    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.session:spring-session-jdbc")
//    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation ("io.grpc:grpc-protobuf:1.73.0")
    implementation ("io.grpc:grpc-stub:1.73.0")
//    implementation ("com.google.protobuf:protobuf-java:3.25.1")
    compileOnly("org.projectlombok:lombok")
    compileOnly ("org.apache.tomcat:annotations-api:6.0.53")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    runtimeOnly ("io.grpc:grpc-netty-shaded:1.73.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor ("org.mapstruct:mapstruct-processor:1.5.5.Final")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.grpc:spring-grpc-dependencies:${property("springGrpcVersion")}")
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.73.0"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc") {
                    option("@generated=omit")
                }
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
