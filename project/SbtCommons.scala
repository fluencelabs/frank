import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport.headerLicense
import de.heikoseeberger.sbtheader.License
import org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtOnCompile
import sbt.Keys.{javaOptions, _}
import sbt.{Def, addCompilerPlugin, _}
import sbtassembly.AssemblyPlugin.autoImport.assemblyMergeStrategy
import sbtassembly.{MergeStrategy, PathList}

import scala.sys.process._

object SbtCommons {

  val scalaV = scalaVersion := "2.12.9"

  val commons = Seq(
    scalaV,
    version                              := "0.1.1",
    fork in Test                         := true,
    parallelExecution in Test            := false,
    fork in IntegrationTest              := true,
    parallelExecution in IntegrationTest := false,
    organizationName                     := "Fluence Labs Limited",
    organizationHomepage                 := Some(new URL("https://fluence.network")),
    startYear                            := Some(2019),
    licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    headerLicense := Some(License.ALv2("2019", organizationName.value)),
    resolvers += Resolver.bintrayRepo("fluencelabs", "releases"),
    scalafmtOnCompile := true,
    // see good explanation https://gist.github.com/djspiewak/7a81a395c461fd3a09a6941d4cd040f2
    scalacOptions ++= Seq("-Ypartial-unification", "-deprecation"),
    javaOptions in Test ++= Seq(
      "-XX:MaxMetaspaceSize=4G",
      "-Xms4G",
      "-Xmx4G",
      "-Xss6M"
    ),
    javaOptions in IntegrationTest ++= Seq(
      "-XX:MaxMetaspaceSize=4G",
      "-Xms4G",
      "-Xmx4G",
      "-Xss6M"
    ),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0")
  )

  val mergeStrategy = Def.setting[String => MergeStrategy]({
    // a module definition fails compilation for java 8, just skip it
    case PathList("module-info.class", xs @ _*)  => MergeStrategy.first
    case "META-INF/io.netty.versions.properties" => MergeStrategy.first
    case x =>
      import sbtassembly.AssemblyPlugin.autoImport.assembly
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }: String => MergeStrategy)

  val rustToolchain = "nightly-2019-09-23"

  def compileFrank() = {
    val projectRoot = file("").getAbsolutePath
    val frankFolder = s"$projectRoot/src/main/rust"
    val localCompileCmd = s"cargo +$rustToolchain build --manifest-path $frankFolder/Cargo.toml --release --lib"
    val crossCompileCmd = s"cd $frankFolder ; cross build --target x86_64-unknown-linux-gnu --release --lib"

    assert((localCompileCmd !) == 0, "Frank VM native compilation failed")
    assert((crossCompileCmd !) == 0, "Cross compilation to linux failed")
  }

  def compileFrankVMSettings(): Seq[Def.Setting[_]] =
    Seq(
      publishArtifact := false,
      test            := (test in Test).dependsOn(compile).value,
      compile := (compile in Compile)
        .dependsOn(Def.task {
          val log = streams.value.log

          log.info("Compiling Frank VM")
          compileFrank()
        })
        .value
    )

  def downloadLlamadb(): Seq[Def.Setting[_]] =
    Seq(
      publishArtifact := false,
      test            := (test in Test).dependsOn(compile).value,
      compile := (compile in Compile)
        .dependsOn(Def.task {
          // by defaults, user.dir in sbt points to a submodule directory while in Idea to the project root
          val resourcesPath = System.getProperty("user.dir") + "/src/it/resources/"

          val log = streams.value.log
          val llamadbUrl = "https://github.com/fluencelabs/llamadb-wasm/releases/download/0.1.2/llama_db.wasm"
          val llamadbPreparedUrl =
            "https://github.com/fluencelabs/llamadb-wasm/releases/download/0.1.2/llama_db_prepared.wasm"

          log.info(s"Dowloading llamadb from $llamadbUrl to $resourcesPath")

          // -nc prevents downloading if file already exists
          val llamadbDownloadRet = s"wget -nc $llamadbUrl -O $resourcesPath/llama_db.wasm" !
          val llamadbPreparedDownloadRet = s"wget -nc $llamadbPreparedUrl -O $resourcesPath/llama_db_prepared.wasm" !

          // wget returns 0 of file was downloaded and 1 if file already exists
          assert(llamadbDownloadRet == 0 || llamadbDownloadRet == 1, s"Download failed: $llamadbUrl")
          assert(
            llamadbPreparedDownloadRet == 0 || llamadbPreparedDownloadRet == 1,
            s"Download failed: $llamadbPreparedUrl"
          )
        })
        .value
    )

  /* Common deps */

  val catsVersion = "2.0.0"
  val cats = "org.typelevel" %% "cats-core" % catsVersion
  val catsEffectVersion = "2.0.0"
  val catsEffect = "org.typelevel" %% "cats-effect" % catsEffectVersion

  // functional wrapper around 'lightbend/config'
  val ficus = "com.iheart" %% "ficus" % "1.4.5"

  // for ByteVector
  val scodecBits = "org.scodec" %% "scodec-bits" % "1.1.9"
  val scodecCore = "org.scodec" %% "scodec-core" % "1.11.3"

  // test deps
  val scalacheckShapeless = "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.8"     % Test
  val catsTestkit = "org.typelevel"                      %% "cats-testkit"              % catsVersion % Test
  val disciplineScalaTest = "org.typelevel"              %% "discipline-scalatest"      % "1.0.0-M1"  % Test

  val scalaTest = "org.scalatest"            %% "scalatest"   % "3.0.8"  % Test
  val scalaIntegrationTest = "org.scalatest" %% "scalatest"   % "3.0.8"  % IntegrationTest
  val mockito = "org.mockito"                % "mockito-core" % "2.21.0" % Test
}
