package org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.result;

import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ColumnDefinition;

public interface CachedResultSetMetaData extends ColumnDefinition {
   java.sql.ResultSetMetaData getMetadata();

   void setMetadata(java.sql.ResultSetMetaData var1);
}
