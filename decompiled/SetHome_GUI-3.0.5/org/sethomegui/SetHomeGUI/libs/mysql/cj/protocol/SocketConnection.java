package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertySet;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionInterceptor;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.FeatureNotAvailableException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.SSLParamsException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.log.Log;

public interface SocketConnection {
   void connect(String var1, int var2, PropertySet var3, ExceptionInterceptor var4, Log var5, int var6);

   void performTlsHandshake(ServerSession var1) throws SSLParamsException, FeatureNotAvailableException, IOException;

   default void performTlsHandshake(ServerSession serverSession, Log log) throws SSLParamsException, FeatureNotAvailableException, IOException {
      this.performTlsHandshake(serverSession);
   }

   void forceClose();

   NetworkResources getNetworkResources();

   String getHost();

   int getPort();

   Socket getMysqlSocket() throws IOException;

   FullReadInputStream getMysqlInput() throws IOException;

   void setMysqlInput(FullReadInputStream var1);

   BufferedOutputStream getMysqlOutput() throws IOException;

   boolean isSSLEstablished();

   SocketFactory getSocketFactory();

   void setSocketFactory(SocketFactory var1);

   ExceptionInterceptor getExceptionInterceptor();

   PropertySet getPropertySet();
}
