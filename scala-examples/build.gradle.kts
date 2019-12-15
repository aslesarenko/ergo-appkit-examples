plugins {
    java
    scala
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
//    val ivyDir = "${System.getProperties().getProperty("user.home")}/.ivy2/local"
//    ivy(ivyDir) {
//        layout("ivy")
//        patternLayout {
//            artifactPattern("${ivyDir}/[organisation]/[module]/[revision]/[type]s/[artifact](-[classifier])(.[ext])")
//        }
//    }
}

dependencies {
    implementation("org.scala-lang:scala-library:2.12.8")
    implementation("org.ergoplatform", "ergo-appkit_2.12", "sandboxed-098db859-SNAPSHOT", "compile")
    implementation("org.graalvm.sdk", "graal-sdk", "19.2.1", "compile")
    implementation("com.squareup.okhttp3:mockwebserver:3.12.0")
//    testImplementation("junit", "junit", "4.12")
    testImplementation("org.scalatest", "scalatest_2.12", "3.0.8")
    testImplementation("org.scalacheck", "scalacheck_2.12", "1.14.2")
    testImplementation("org.ergoplatform", "ergo-appkit_2.12", "sandboxed-098db859-SNAPSHOT", classifier = "tests")

}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}