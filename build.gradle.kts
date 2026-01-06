import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.19")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.9.20")
    }
}

repositories {
    mavenCentral()
}
apply(from = "gradle/gradle-mvn-push.gradle")
apply(plugin = "idea")

plugins {
    kotlin("jvm") version "2.3.0"
    `maven-publish`
    `java-library`
    id("org.jetbrains.dokka") version "1.9.20"
    id("io.codearte.nexus-staging") version "0.30.0"
    id("com.google.protobuf") version "0.8.19"
    id("com.diffplug.spotless") version "6.21.0"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
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
val protoSrc = File("$projectDir/protocol/protobufs/").listFiles { f -> f.isFile }

sourceSets {
    main {
        proto {
            srcDir("$projectDir/protocol/protobufs/")
            exclude("agent/*")
            exclude("infra/*")
            exclude("rpc/*")
        }
    }
}

val protobufVersion = "4.29.4"
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
                    URI(
                        "https://github.com/livekit/server-sdk-kotlin/tree/main/src/main/kotlin"
                    ).toURL()
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

    implementation(platform("com.squareup.okhttp3:okhttp-bom:5.3.2"))
    implementation("com.squareup.okhttp3:logging-interceptor")
    api("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-protobuf:3.0.0")
    implementation("com.auth0:java-jwt:4.5.0")
    api(protobufDep)
    api("com.google.protobuf:protobuf-java-util:$protobufVersion")
    implementation("javax.annotation:javax.annotation-api:1.3.2")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

group = properties["GROUP"].toString()
version = properties["VERSION_NAME"].toString()

nexusPublishing {
    repositories {
        sonatype {
            //only for users registered in Sonatype after 24 Feb 2021
            username.set(properties["nexusUsername"].toString())
            password.set(properties["nexusPassword"].toString())
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
        }
    }
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
