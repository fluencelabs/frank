import SbtCommons._

name := "frank"

commons

/* Projects */

lazy val root = (project in file("."))
  .aggregate(`vm-scala`, `vm-rust`)

lazy val `vm-rust` = (project in file("vm/src/main/rust/"))
  .settings(
    compileFrankVMSettings()
  )

lazy val `vm-llamadb` = (project in file("vm/src/it/resources/llamadb"))
  .settings(
    downloadLlamadb()
  )

lazy val `vm-scala` = (project in file("vm"))
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    commons,
    libraryDependencies ++= Seq(
      cats,
      catsEffect,
      ficus,
      cryptoHashsign,
      scalaTest,
      scalaIntegrationTest,
      mockito
    ),
    assemblyJarName in assembly       := "frank.jar",
    assemblyMergeStrategy in assembly := SbtCommons.mergeStrategy.value,
    test in assembly                  := {},
    compile in Compile := (compile in Compile)
      .dependsOn(compile in `vm-rust`).value,
    test in IntegrationTest := (test in IntegrationTest)
      .dependsOn(compile in `vm-llamadb`)
      .value
  )
  .enablePlugins(AutomateHeaderPlugin)
