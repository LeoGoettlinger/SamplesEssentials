package org.jdbi.v3.core.argument;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;

class SqlArgumentFactory extends DelegatingArgumentFactory {
   SqlArgumentFactory() {
      this.register(Blob.class, 2004, PreparedStatement::setBlob);
      this.register(Clob.class, 2005, PreparedStatement::setClob);
   }
}
