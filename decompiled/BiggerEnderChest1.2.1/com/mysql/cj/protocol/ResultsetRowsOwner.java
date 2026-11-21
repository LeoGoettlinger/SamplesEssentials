package com.mysql.cj.protocol;

import com.mysql.cj.MysqlConnection;
import com.mysql.cj.Query;
import com.mysql.cj.Session;
import java.util.concurrent.locks.Lock;

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
