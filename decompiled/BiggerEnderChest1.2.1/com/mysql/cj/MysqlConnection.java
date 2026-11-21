package com.mysql.cj;

import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.protocol.ServerSessionStateController;
import java.util.Properties;
import java.util.concurrent.locks.Lock;

public interface MysqlConnection {
   PropertySet getPropertySet();

   void createNewIO(boolean var1);

   long getId();

   Properties getProperties();

   Lock getConnectionLock();

   Session getSession();

   String getURL();

   String getUser();

   ExceptionInterceptor getExceptionInterceptor();

   void checkClosed();

   void cleanup(Throwable var1);

   ServerSessionStateController getServerSessionStateController();
}
