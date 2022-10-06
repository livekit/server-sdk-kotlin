import com.google.protobuf.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.19")
    }
}

plugins {
    kotlin("jvm") version "1.7.10"
    id("idea")
    application
    id("com.google.protobuf") version "0.8.19"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

sourceSets {
    val grpc = create("grpc") {
    }

    getByName("test") {
        compileClasspath += grpc.output
        runtimeClasspath += grpc.output
    }
}

val grpcVersion = "1.49.2"

val protoc_platform: String? by project
val protoSrc = "$projectDir/protocol"
val protobufVersion = "3.21.7"
val protobufDep = "com.google.protobuf:protobuf-java:$protobufVersion"
protobuf {
    protoc {
        // for apple m1, please add protoc_platform=osx-x86_64 in $HOME/.gradle/gradle.properties
        if (protoc_platform != null) {
            artifact = "com.google.protobuf:protoc:$protobufVersion:$protoc_platform"
        } else {
            artifact = "com.google.protobuf:protoc:$protobufVersion"
        }
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
    }

    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                // Apply the "grpc" plugin whose spec is defined above, without
                // options. Note the braces cannot be omitted, otherwise the
                // plugin will not be added. This is because of the implicit way
                // NamedDomainObjectContainer binds the methods.
                id("grpc") { }
            }
        }
    }
}

val grpcImplementation = configurations.getByName("grpcImplementation")

dependencies {
    protobuf(files(protoSrc))
    runtimeOnly("io.grpc:grpc-netty-shaded:1.49.2")

    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation(protobufDep)
    implementation("javax.annotation:javax.annotation-api:1.3.2")

    grpcImplementation("io.grpc:grpc-stub:$grpcVersion")
    grpcImplementation("io.grpc:grpc-protobuf:$grpcVersion")
    grpcImplementation(protobufDep)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}