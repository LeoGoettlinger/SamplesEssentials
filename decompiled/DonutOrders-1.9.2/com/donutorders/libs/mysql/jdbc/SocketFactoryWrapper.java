package com.donutorders.libs.mysql.jdbc;

import com.donutorders.libs.mysql.cj.conf.PropertySet;
import com.donutorders.libs.mysql.cj.log.Log;
import com.donutorders.libs.mysql.cj.protocol.ServerSession;
import com.donutorders.libs.mysql.cj.protocol.SocketConnection;
import com.donutorders.libs.mysql.cj.protocol.StandardSocketFactory;
import java.io.Closeable;
import java.io.IOException;

public class SocketFactoryWrapper extends StandardSocketFactory implements com.donutorders.libs.mysql.cj.protocol.SocketFactory {
   SocketFactory socketFactory;

   public SocketFactoryWrapper(SocketFactory legacyFactory) {
      this.socketFactory = legacyFactory;
   }

   @Override
   public <T extends Closeable> T connect(String hostname, int portNumber, PropertySet pset, int loginTimeout) throws IOException {
      this.rawSocket = this.socketFactory.connect(hostname, portNumber, pset.exposeAsProperties());
      return (T)this.rawSocket;
   }

   @Override
   public <T extends Closeable> T performTlsHandshake(SocketConnection socketConnection, ServerSession serverSession, Log log) throws IOException {
      return super.performTlsHandshake(socketConnection, serverSession, log);
   }

   @Override
   public void beforeHandshake() throws IOException {
      this.socketFactory.beforeHandshake();
   }

   @Override
   public void afterHandshake() throws IOException {
      this.socketFactory.afterHandshake();
   }
}
