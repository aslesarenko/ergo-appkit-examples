plugins {
    java
}

group = "org.ergoplatform"
version = "3.1.0-SNAPSHOT"

//val ivyDir = "${System.getProperties().getProperty("user.home")}/.ivy2/local"

repositories {
    mavenCentral()
//    mavenLocal()
//    maven("https://oss.sonatype.org/content/repositories/snapshots/")
//    ivy("${System.getProperties().getProperty("user.home")}/.ivy2/local") {
//        layout("ivy") {
//            artifactPattern("${ivyDir}/[organisation]/[module]/[revision]/[type]s/[artifact](-[classifier])(.[ext])")
//        }
//    }
}

dependencies {
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}