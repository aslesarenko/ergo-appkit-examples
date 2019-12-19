plugins {
    java
    idea
}

idea {
    module {
        isDownloadSources = true
    }
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/releases/")

// Uncomment this to use local Ivy repository (e.g. when ergo-appkit is published locally using `sbt publishLocal`)
    val ivyDir = "${System.getProperties().getProperty("user.home")}/.ivy2/local"
    ivy(ivyDir) {
        layout("ivy")
        patternLayout {
            artifactPattern("${ivyDir}/[organisation]/[module]/[revision]/[type]s/[artifact](-[classifier])(.[ext])")
        }
    }
}

dependencies {
    testImplementation("junit", "junit", "4.12")
    implementation("org.ergoplatform", "ergo-appkit_2.12", "sandboxed-18e15e59-SNAPSHOT", "compile")
//    implementation("org.ergoplatform", "ergo-appkit_2.12", "3.1.0", "compile")
    implementation("org.graalvm.sdk", "graal-sdk", "19.2.1", "compile")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}