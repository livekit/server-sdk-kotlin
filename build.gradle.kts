import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.19")
        classpath("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.30.0")
    }
}

repositories {
    mavenCentral()
}
apply(from = "gradle/gradle-mvn-push.gradle")

plugins {
    kotlin("jvm") version "1.7.10"
    `maven-publish`
    `java-library`
    id("io.codearte.nexus-staging") version "0.30.0"
    id("com.google.protobuf") version "0.8.19"
}


java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

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
}

dependencies {
    protobuf(files(protoSrc))
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    api("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-protobuf:2.9.0")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
    api(protobufDep)
    api("com.google.protobuf:protobuf-java-util:$protobufVersion")
    implementation("javax.annotation:javax.annotation-api:1.3.2")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

nexusStaging {
    serverUrl = "https://s01.oss.sonatype.org/service/local/"
    packageGroup = properties["GROUP"] as String
    stagingProfileId = "16b57cbf143daa"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = properties["POM_ARTIFACT_ID"] as String
            version = properties["VERSION_NAME"] as String

            from(components["java"])
        }
    }
}