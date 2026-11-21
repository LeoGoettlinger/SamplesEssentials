package com.donutorders.libs.mysql.cj.protocol;

import com.donutorders.libs.mysql.cj.MysqlConnection;
import com.donutorders.libs.mysql.cj.Query;
import com.donutorders.libs.mysql.cj.Session;

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
