import slick.jdbc.MySQLProfile.api._
import com.liyaos.forklift.slick.SqlMigration

object M1 {
  MyMigrations.migrations = MyMigrations.migrations :+ SqlMigration(1)(List(
    sqlu"""
    CREATE TABLE IF NOT EXISTS user (
      id INT AUTO_INCREMENT PRIMARY KEY,
      first_name VARCHAR(255),
      last_name VARCHAR(255),
      email VARCHAR(255) NOT NULL UNIQUE,
      password_hash VARCHAR(255) NOT NULL,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );
    """,
    sqlu"""
    CREATE TABLE IF NOT EXISTS token (
      id INT AUTO_INCREMENT PRIMARY KEY,
      user_id INT NOT NULL,
      token VARCHAR(255) NOT NULL,
      expires_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      FOREIGN KEY (user_id)
        REFERENCES user(id)
        ON DELETE CASCADE
    );
    """
  ))
}
