
lazy val allConfigDependency = "compile->compile;test->test"

version := "3.1.0"

libraryDependencies ++= Seq(
  "org.ergoplatform" %% "ergo-appkit" % "sandboxed-098db859-SNAPSHOT" % allConfigDependency,
  "org.graalvm.sdk" % "graal-sdk" % "19.2.1",
  "com.squareup.okhttp3" % "mockwebserver" % "3.12.0",

  "org.scalatest" %% "scalatest" % "3.0.8" % "test",
  "org.scalacheck" %% "scalacheck" % "1.14.1" % "test"
)

publishMavenStyle in ThisBuild := true

publishArtifact in Test := false

pomExtra in ThisBuild :=
  <developers>
    <developer>
      <id>aslesarenko</id>
      <name>Alexander Slesarenko</name>
      <url>https://github.com/aslesarenko/</url>
    </developer>
  </developers>

// set bytecode version to 8 to fix NoSuchMethodError for various ByteBuffer methods
// see https://github.com/eclipse/jetty.project/issues/3244
// these options applied only in "compile" task since scalac crashes on scaladoc compilation with "-release 8"
// see https://github.com/scala/community-builds/issues/796#issuecomment-423395500
scalacOptions in(Compile, compile) ++= (if (scalaBinaryVersion.value == "2.11") Seq() else Seq("-release", "8"))

assemblyJarName in assembly := s"appkit-scala-examples-${version.value}.jar"

