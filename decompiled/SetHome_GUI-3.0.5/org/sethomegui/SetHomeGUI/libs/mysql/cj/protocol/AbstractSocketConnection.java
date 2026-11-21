package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Messages;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertySet;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionInterceptor;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.UnableToConnectException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.WrongArgumentException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.util.Util;
import org.sethomegui.SetHomeGUI.libs.mysql.jdbc.SocketFactoryWrapper;

public abstract class AbstractSocketConnection implements SocketConnection {
   protected String host = null;
   protected int port = 3306;
   protected SocketFactory socketFactory = null;
   protected Socket mysqlSocket = null;
   protected FullReadInputStream mysqlInput = null;
   protected BufferedOutputStream mysqlOutput = null;
   protected ExceptionInterceptor exceptionInterceptor;
   protected PropertySet propertySet;

   @Override
   public String getHost() {
      return this.host;
   }

   @Override
   public int getPort() {
      return this.port;
   }

   @Override
   public Socket getMysqlSocket() {
      return this.mysqlSocket;
   }

   @Override
   public FullReadInputStream getMysqlInput() throws IOException {
      if (this.mysqlInput != null) {
         return this.mysqlInput;
      } else {
         throw new IOException(Messages.getString("SocketConnection.1"));
      }
   }

   @Override
   public void setMysqlInput(FullReadInputStream mysqlInput) {
      this.mysqlInput = mysqlInput;
   }

   @Override
   public BufferedOutputStream getMysqlOutput() throws IOException {
      if (this.mysqlOutput != null) {
         return this.mysqlOutput;
      } else {
         throw new IOException(Messages.getString("SocketConnection.1"));
      }
   }

   @Override
   public boolean isSSLEstablished() {
      return ExportControlled.enabled() && ExportControlled.isSSLEstablished(this.getMysqlSocket());
   }

   @Override
   public SocketFactory getSocketFactory() {
      return this.socketFactory;
   }

   @Override
   public void setSocketFactory(SocketFactory socketFactory) {
      this.socketFactory = socketFactory;
   }

   @Override
   public void forceClose() {
      try {
         this.getNetworkResources().forceClose();
      } finally {
         this.mysqlSocket = null;
         this.mysqlInput = null;
         this.mysqlOutput = null;
      }
   }

   @Override
   public NetworkResources getNetworkResources() {
      return new NetworkResources(this.mysqlSocket, this.mysqlInput, this.mysqlOutput);
   }

   @Override
   public ExceptionInterceptor getExceptionInterceptor() {
      return this.exceptionInterceptor;
   }

   @Override
   public PropertySet getPropertySet() {
      return this.propertySet;
   }

   protected SocketFactory createSocketFactory(String socketFactoryClassName) {
      if (socketFactoryClassName == null) {
         throw (UnableToConnectException)ExceptionFactory.createException(
            UnableToConnectException.class, Messages.getString("SocketConnection.0"), this.getExceptionInterceptor()
         );
      } else {
         try {
            return Util.getInstance(SocketFactory.class, socketFactoryClassName, null, null, this.getExceptionInterceptor());
         } catch (WrongArgumentException var5) {
            if (var5.getCause() == null) {
               try {
                  return new SocketFactoryWrapper(
                     Util.getInstance(
                        org.sethomegui.SetHomeGUI.libs.mysql.jdbc.SocketFactory.class, socketFactoryClassName, null, null, this.getExceptionInterceptor()
                     )
                  );
               } catch (Exception var4) {
                  throw var5;
               }
            } else {
               throw var5;
            }
         }
      }
   }
}
