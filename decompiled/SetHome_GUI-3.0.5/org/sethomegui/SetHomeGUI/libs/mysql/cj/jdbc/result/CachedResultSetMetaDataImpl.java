package org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.result;

import org.sethomegui.SetHomeGUI.libs.mysql.cj.result.DefaultColumnDefinition;

public class CachedResultSetMetaDataImpl extends DefaultColumnDefinition implements CachedResultSetMetaData {
   java.sql.ResultSetMetaData metadata;

   @Override
   public java.sql.ResultSetMetaData getMetadata() {
      return this.metadata;
   }

   @Override
   public void setMetadata(java.sql.ResultSetMetaData metadata) {
      this.metadata = metadata;
   }
}
