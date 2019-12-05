
plugins {
    // See https://imperceptiblethoughts.com/shadow/getting-started/#default-java-groovy-tasks
    // use `./gradlew shadowJar` to generate fat jar with java examples and all Appkit dependencies
    id("com.github.johnrengelman.shadow") version "5.2.0"
    java
    idea
}

group = "org.ergoplatform"
version = "3.1.0"  // first two numbers correspond to appkit release

val ivyDir = "${System.getProperties().getProperty("user.home")}/.ivy2/local"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")

    // This is requred to use local Ivy repo (e.g. when Appkit is cloned and `sbt publishLocal` used)
    ivy(ivyDir) {
        layout("ivy")
        patternLayout {
            artifactPattern("${ivyDir}/[organisation]/[module]/[revision]/[type]s/[artifact](-[classifier])(.[ext])")
        }
    }
}
dependencies {
  // the root project doesn't have Java code, but this dependency is required for fat jar generation
  implementation(project(":java-examples"))
  implementation(project(":scala-examples"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

