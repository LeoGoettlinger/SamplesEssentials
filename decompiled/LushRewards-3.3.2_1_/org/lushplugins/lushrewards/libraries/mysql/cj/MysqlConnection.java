package org.lushplugins.lushrewards.libraries.mysql.cj;

import java.util.Properties;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionInterceptor;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ServerSessionStateController;

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
