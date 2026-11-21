package org.postgresql.core;

import java.util.Properties;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import org.postgresql.PGProperty;
import org.postgresql.ssl.LibPQFactory;
import org.postgresql.util.GT;
import org.postgresql.util.ObjectFactory;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public class SocketFactoryFactory {
   public static SocketFactory getSocketFactory(Properties info) throws PSQLException {
      String socketFactoryClassName = PGProperty.SOCKET_FACTORY.getOrDefault(info);
      if (socketFactoryClassName == null) {
         return SocketFactory.getDefault();
      } else {
         try {
            return ObjectFactory.instantiate(SocketFactory.class, socketFactoryClassName, info, true, PGProperty.SOCKET_FACTORY_ARG.getOrDefault(info));
         } catch (Exception var3) {
            throw new PSQLException(
               GT.tr("The SocketFactory class provided {0} could not be instantiated.", socketFactoryClassName), PSQLState.CONNECTION_FAILURE, var3
            );
         }
      }
   }

   public static SSLSocketFactory getSslSocketFactory(Properties info) throws PSQLException {
      String classname = PGProperty.SSL_FACTORY.getOrDefault(info);
      if (classname != null && !"org.postgresql.ssl.jdbc4.LibPQFactory".equals(classname) && !"org.postgresql.ssl.LibPQFactory".equals(classname)) {
         try {
            return ObjectFactory.instantiate(SSLSocketFactory.class, classname, info, true, PGProperty.SSL_FACTORY_ARG.getOrDefault(info));
         } catch (Exception var3) {
            throw new PSQLException(GT.tr("The SSLSocketFactory class provided {0} could not be instantiated.", classname), PSQLState.CONNECTION_FAILURE, var3);
         }
      } else {
         return new LibPQFactory(info);
      }
   }
}
