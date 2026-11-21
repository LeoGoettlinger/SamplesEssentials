package org.sethomegui.SetHomeGUI.libs.mysql.cj;

import java.util.Properties;
import java.util.concurrent.locks.Lock;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertySet;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionInterceptor;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ServerSessionStateController;

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
