
plugins {
    id("com.github.johnrengelman.shadow") version "5.2.0"
    java
}

group = "org.ergoplatform"
version = "3.1.0"  // first two numbers correspond to appkit release

val ivyDir = "${System.getProperties().getProperty("user.home")}/.ivy2/local"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    ivy(ivyDir) {
        layout("ivy")
        patternLayout {
            artifactPattern("${ivyDir}/[organisation]/[module]/[revision]/[type]s/[artifact](-[classifier])(.[ext])")
        }
    }
}

dependencies {
  implementation(project(":java-examples"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

