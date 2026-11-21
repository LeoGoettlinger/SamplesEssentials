package org.lushplugins.lushrewards.libraries.mysql.cj.protocol;

import org.lushplugins.lushrewards.libraries.mysql.cj.MysqlConnection;
import org.lushplugins.lushrewards.libraries.mysql.cj.Query;
import org.lushplugins.lushrewards.libraries.mysql.cj.Session;

public interface ResultsetRowsOwner {
   void closeOwner(boolean var1);

   MysqlConnection getConnection();

   Session getSession();

   Object getSyncMutex();

   String getPointOfOrigin();

   int getOwnerFetchSize();

   Query getOwningQuery();

   int getOwningStatementMaxRows();

   int getOwningStatementFetchSize();

   long getOwningStatementServerId();
}
