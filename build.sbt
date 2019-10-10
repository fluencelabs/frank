import SbtCommons._

name := "frank"

commons

skip in publish := false // skip the root project to be published

/* Projects */

lazy val `vm-rust` = (project in file("src/main/rust"))
  .settings(
    skip in publish := true,
    compileFrankVMSettings()
  )

lazy val `vm-llamadb` = (project in file("src/it/resources/llamadb"))
  .settings(
    skip in publish := true,
    downloadLlamadb()
  )

lazy val `vm-scala` = (project in file("."))
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    commons,
    libraryDependencies ++= Seq(
      cats,
      catsEffect,
      ficus,
      scalaTest,
      scalaIntegrationTest,
      scodecCore,
      mockito
    ),
    nativeResourceSettings(),
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
