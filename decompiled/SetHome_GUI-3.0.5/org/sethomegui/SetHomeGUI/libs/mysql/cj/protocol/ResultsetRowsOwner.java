package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol;

import java.util.concurrent.locks.Lock;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.MysqlConnection;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Query;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Session;

public interface ResultsetRowsOwner {
   void closeOwner();

   MysqlConnection getConnection();

   Session getSession();

   Lock getLock();

   String getPointOfOrigin();

   int getOwnerFetchSize();

   Query getOwningQuery();

   int getOwningStatementMaxRows();

   int getOwningStatementFetchSize();

   long getOwningStatementServerId();
}
