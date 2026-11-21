package org.postgresql.xa;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.Reference;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.BaseConnection;
import org.postgresql.ds.common.BaseDataSource;

public class PGXADataSource extends BaseDataSource implements XADataSource {
   @Override
   public XAConnection getXAConnection() throws SQLException {
      return this.getXAConnection(this.getUser(), this.getPassword());
   }

   @Override
   public XAConnection getXAConnection(@Nullable String user, @Nullable String password) throws SQLException {
      Connection con = super.getConnection(user, password);
      return new PGXAConnection((BaseConnection)con);
   }

   @Override
   public String getDescription() {
      return "XA-enabled DataSource from PostgreSQL JDBC Driver 42.7.3";
   }

   @Override
   protected Reference createReference() {
      return new Reference(this.getClass().getName(), PGXADataSourceFactory.class.getName(), null);
   }
}
