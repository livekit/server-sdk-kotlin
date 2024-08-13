import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.19")
        classpath("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.30.0")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.9.20")
    }
}

repositories {
    mavenCentral()
}
apply(from = "gradle/gradle-mvn-push.gradle")

plugins {
    kotlin("jvm") version "1.9.0"
    `maven-publish`
    `java-library`
    id("org.jetbrains.dokka") version "1.9.20"
    id("io.codearte.nexus-staging") version "0.30.0"
    id("com.google.protobuf") version "0.8.19"
    id("com.diffplug.spotless") version "6.21.0"
}


java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

spotless {
    // optional: limit format enforcement to just the files changed by this feature branch
    ratchetFrom("origin/main")

    format("misc") {
        // define the files to apply `misc` to
        target("*.gradle", "*.md", ".gitignore")

        // define the steps to apply to those files
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
    java {
        // apply a specific flavor of google-java-format
        googleJavaFormat("1.17.0").aosp().reflowLongStrings()
        // fix formatting of type annotations
        formatAnnotations()
        // make sure every file has the following copyright header.
        // optionally, Spotless can set copyright years by digging
        // through git history (see "license" section below)
        licenseHeaderFile(rootProject.file("LicenseHeaderFile.txt"))
        removeUnusedImports()
        toggleOffOn()
    }
    kotlin {
        target("src/*/java/**/*.kt", "src/*/kotlin/**/*.kt")
        ktlint("0.50.0")
            .setEditorConfigPath("$rootDir/.editorconfig")
        licenseHeaderFile(rootProject.file("LicenseHeaderFile.txt"))
            .named("license")
        endWithNewline()
        toggleOffOn()
    }
}

val protoc_platform: String? by project
val protoSrc = arrayOf(
    "$projectDir/protocol/protobufs/livekit_agent.proto",
    "$projectDir/protocol/protobufs/livekit_agent_dispatch.proto",
    "$projectDir/protocol/protobufs/livekit_analytics.proto",
    "$projectDir/protocol/protobufs/livekit_egress.proto",
    "$projectDir/protocol/protobufs/livekit_ingress.proto",
    "$projectDir/protocol/protobufs/livekit_internal.proto",
    "$projectDir/protocol/protobufs/livekit_models.proto",
    "$projectDir/protocol/protobufs/livekit_room.proto",
    "$projectDir/protocol/protobufs/livekit_rpc_internal.proto",
    "$projectDir/protocol/protobufs/livekit_rtc.proto",
    "$projectDir/protocol/protobufs/livekit_webhook.proto",
)
val protobufVersion = "3.21.7"
val protobufDep = "com.google.protobuf:protobuf-java:$protobufVersion"
protobuf {
    protoc {
        // for apple m1, please add protoc_platform=osx-x86_64 in $HOME/.gradle/gradle.properties
        artifact = if (protoc_platform != null) {
            "com.google.protobuf:protoc:$protobufVersion:$protoc_platform"
        } else {
            "com.google.protobuf:protoc:$protobufVersion"
        }
    }
}

fun org.jetbrains.dokka.gradle.DokkaTask.configureDokkaTask() {
    moduleName.set("livekit-server-sdk")
    dokkaSourceSets {
        configureEach {
            skipEmptyPackages.set(true)
            includeNonPublic.set(false)
            includes.from("module.md")
            displayName.set("SDK")
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))

                // URL showing where the source code can be accessed through the web browser
                remoteUrl.set(
                    URL(
                        "https://github.com/livekit/server-sdk-kotlin/tree/main/src/main/kotlin"
                    )
                )
                // Suffix which is used to append the line number to the URL. Use #L for GitHub
                remoteLineSuffix.set("#L")
            }
        }
    }
}

tasks.dokkaHtml.configure {
    configureDokkaTask()
}

tasks.dokkaJavadoc.configure {
    configureDokkaTask()
}
val javadocJar = tasks.named<Jar>("javadocJar") {
    from(tasks.named("dokkaJavadoc"))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

dependencies {
    protobuf(files(*protoSrc))
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    api("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-protobuf:2.9.0")
    implementation("com.auth0:java-jwt:4.2.1")
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
