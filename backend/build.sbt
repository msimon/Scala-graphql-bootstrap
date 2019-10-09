addCommandAlias("mgm", "migration_manager/run")
addCommandAlias("mg", "migrations/run")

lazy val commonSettings = Seq(
  scalaVersion := "2.12.10",
  scalacOptions ++= Seq("-deprecation", "-feature"),
  resolvers ++= Seq(
    Resolver.bintrayRepo("naftoligug", "maven"),
    Resolver.sonatypeRepo("snapshots")
  )
)

lazy val slickVersion = "3.2.3"
lazy val forkliftVersion = "0.3.1"

lazy val akkaHttpDependency = Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.1",
  "de.heikoseeberger" %% "akka-http-circe" % "1.20.1",
)

lazy val loggingDependencies = Seq(
  "org.slf4j" % "slf4j-nop" % "1.6.4" // <- disables logging
)

lazy val slickDependencies = Seq(
  "com.typesafe.slick" %% "slick" % slickVersion
)

lazy val dbDependencies = Seq(
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "mysql" % "mysql-connector-java" % "8.0.15",
)

lazy val forkliftDependencies = Seq(
  "com.liyaos" %% "scala-forklift-slick" % forkliftVersion,
  "io.github.nafg" %% "slick-migration-api" % "0.7.0"
)

lazy val appDependencies = akkaHttpDependency ++ dbDependencies ++ loggingDependencies

lazy val migrationsDependencies = akkaHttpDependency ++ dbDependencies ++ forkliftDependencies ++ loggingDependencies
lazy val migrationManagerDependencies = dbDependencies ++ forkliftDependencies


lazy val scalaReflect = Def.setting { "org.scala-lang" % "scala-reflect" % scalaVersion.value }

lazy val root = (project in file("."))
  .settings(
    commonSettings,
    run := (run in Compile in app).evaluated
).aggregate(macroSub, app)
  .disablePlugins(RevolverPlugin)


lazy val app = (project in file("app"))
  .dependsOn(macroSub, generatedCode)
  .settings(
  commonSettings,
    name := "Crypto Seller",
    version := "0.1.0-SNAPSHOT",
    description := "Crypto seller backend implementation",
    libraryDependencies ++= appDependencies ++ Seq(
      "org.sangria-graphql" %% "sangria" % "1.4.0",
      "org.sangria-graphql" %% "sangria-circe" % "1.2.1",

      "org.scalaj" %% "scalaj-http" % "2.4.0",

      "io.circe" %%	"circe-core" % "0.9.3",
      "io.circe" %% "circe-parser" % "0.9.3",
      "io.circe" %% "circe-generic" % "0.9.3",
      "io.circe" %% "circe-optics" % "0.9.3",

      "org.scalatest" %% "scalatest" % "3.0.5" % Test,
      "org.scala-lang" % "scala-reflect" % "2.12.6",
      "org.mindrot" % "jbcrypt" % "0.4",
      "joda-time" % "joda-time" % "2.10.5",
    ),
    addCompilerPlugin(
      "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
    ),
    Revolver.settings,
  )

lazy val macroSub = (project in file("macro"))
  .settings(
  commonSettings,
    name := "Crypto Seller Macro",
    version := "0.1.0-SNAPSHOT",
    description := "Crypto Seller macro implementation",
    libraryDependencies += scalaReflect.value,
  ).disablePlugins(RevolverPlugin)


lazy val migrationManager = (project in file("migration_manager"))
  .settings(
  commonSettings,
    libraryDependencies ++= migrationManagerDependencies,
)


lazy val migrations = (project in file("migrations"))
  .dependsOn(generatedCode, migrationManager)
  .settings(
  commonSettings,
    libraryDependencies ++= migrationsDependencies,
  )

lazy val tools = Project("git-tools",
  file("tools/git")).settings(commonSettings:_*).settings {
  libraryDependencies ++= forkliftDependencies ++ List(
    "com.liyaos" %% "scala-forklift-git-tools" % forkliftVersion,
    "com.typesafe" % "config" % "1.3.0",
    "org.eclipse.jgit" % "org.eclipse.jgit" % "4.0.1.201506240215-r"
  )
}


lazy val generatedCode = Project("generate_code",
  file("generated_code")).settings(commonSettings:_*).settings {
  libraryDependencies ++= slickDependencies
}


enablePlugins(JavaAppPackaging)
