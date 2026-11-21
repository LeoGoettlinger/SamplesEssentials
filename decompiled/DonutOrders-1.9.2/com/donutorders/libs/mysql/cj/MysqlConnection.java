package com.donutorders.libs.mysql.cj;

import com.donutorders.libs.mysql.cj.conf.PropertySet;
import com.donutorders.libs.mysql.cj.exceptions.ExceptionInterceptor;
import com.donutorders.libs.mysql.cj.protocol.ServerSessionStateController;
import java.util.Properties;

public interface MysqlConnection {
   PropertySet getPropertySet();

   void createNewIO(boolean var1);

   long getId();

   Properties getProperties();

   Object getConnectionMutex();

   Session getSession();

   String getURL();

   String getUser();

   ExceptionInterceptor getExceptionInterceptor();

   void checkClosed();

   void normalClose();

   void cleanup(Throwable var1);

   ServerSessionStateController getServerSessionStateController();
}
