package ch.njol.skript.variables;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.log.SkriptLogger;
import lib.PatPeter.SQLibrary.Database;
import lib.PatPeter.SQLibrary.MySQL;

public class MySQLStorage extends SQLStorage {
   MySQLStorage(String type) {
      super(
         type,
         "CREATE TABLE IF NOT EXISTS %s (rowid        BIGINT  NOT NULL  AUTO_INCREMENT  PRIMARY KEY,name         VARCHAR(380)  NOT NULL  UNIQUE,type         VARCHAR(50),value        BLOB(10000),update_guid  CHAR(36)  NOT NULL) CHARACTER SET ucs2 COLLATE ucs2_bin"
      );
   }

   @Override
   public Database initialize(SectionNode config) {
      String host = this.getValue(config, "host");
      Integer port = this.getValue(config, "port", Integer.class);
      String user = this.getValue(config, "user");
      String password = this.getValue(config, "password");
      String database = this.getValue(config, "database");
      this.setTableName(config.get("table", "variables21"));
      return host != null && port != null && user != null && password != null && database != null
         ? new MySQL(SkriptLogger.LOGGER, "[Skript]", host, port, database, user, password)
         : null;
   }

   @Override
   protected boolean requiresFile() {
      return false;
   }
}
