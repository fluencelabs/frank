import SbtCommons._
import ch.jodersky.sbt.jni.plugins.JniPackage.autoImport._

name := "frank"

commons

/* Projects */

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
      scalaTest,
      scalaIntegrationTest,
      scodecCore,
      mockito
    ),
    enableNativeCompilation := false,
  /*  unmanagedNativeDirectories := Seq(
      new File(s"${file("").getAbsolutePath}/vm/src/main/rust/target/release"),
      new File(s"${file("").getAbsolutePath}/vm/src/main/rust/target/x86_64-unknown-linux-gnu/release"),
    ),
    */
    resourceGenerators in Compile += Def.task {
      val managedResource = s"${(resourceManaged in Compile).value}/native"

      val darwin_lib = new File(managedResource + "/darwin_x86_64/libfrank.dylib")
      val linux_lib = new File(managedResource + "/linux_x86_64/libfrank.so")

      IO.copyFile(
        new File(s"${file("").getAbsolutePath}/vm/src/main/rust/target/release/libfrank.dylib"),
        darwin_lib
      )
      IO.copyFile(
        new File(s"${file("").getAbsolutePath}/vm/src/main/rust/target/x86_64-unknown-linux-gnu/release/libfrank.so"),
        linux_lib
      )
      Seq(darwin_lib, linux_lib)
    }.taskValue,
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
