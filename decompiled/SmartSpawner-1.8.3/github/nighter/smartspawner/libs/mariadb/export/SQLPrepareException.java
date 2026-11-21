package github.nighter.smartspawner.libs.mariadb.export;

import java.sql.SQLException;

public class SQLPrepareException extends SQLException {
   private static final long serialVersionUID = 1L;

   public SQLPrepareException(String reason, String sqlState, int vendorCode, Throwable cause) {
      super(reason, sqlState, vendorCode, cause);
   }
}
