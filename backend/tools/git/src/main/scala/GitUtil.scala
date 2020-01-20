import java.io.File
import scala.sys.process._
import com.typesafe.config._
import com.liyaos.forklift.slick.tools.git.H2MigrationDatabase
import com.liyaos.forklift.core.tools.{GitUtil => Git}
import com.liyaos.forklift.tools.git.Installer

class MyGitUtil(db: H2MigrationDatabase)
    extends Git(db, System.getProperty("user.dir") + "/.git") {
  override def run(args: List[String]) {
    args match {
      case "install" :: Nil =>
        val currentDir = System.getProperty("user.dir")
        Installer.install("which sbt".!!.trim, currentDir + "/.git",
          currentDir, "git-tools")
      case "rebuild" :: Nil =>
        db.rebuild()
      case _ =>
        super.run(args)
    }
  }
}

object GitUtil {
  private val config = ConfigFactory.load()
  val dbLoc = config.getString("slick.db.url")
  val objLoc = config.getString("slick.version_control_dir")

  def main(args: Array[String]) {
    val db = new H2MigrationDatabase(dbLoc, objLoc)
    val tool = new MyGitUtil(db)
    tool.run(args.toList)
  }
}
