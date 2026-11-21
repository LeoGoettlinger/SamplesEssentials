package org.lushplugins.lushrewards.libraries.mysql.cj.jdbc.result;

import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ColumnDefinition;

public interface CachedResultSetMetaData extends ColumnDefinition {
   java.sql.ResultSetMetaData getMetadata();

   void setMetadata(java.sql.ResultSetMetaData var1);
}
