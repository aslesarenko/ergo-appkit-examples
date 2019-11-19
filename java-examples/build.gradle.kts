plugins {
    java
}

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
    testImplementation("junit", "junit", "4.12")
    implementation("org.ergoplatform", "ergo-appkit_2.12", "3.1.0", "compile")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}