package org.lushplugins.lushrewards.libraries.mysql.cj.xdevapi;

import java.util.Properties;
import org.lushplugins.lushrewards.libraries.mysql.cj.Messages;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.ConnectionUrl;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.HostInfo;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertyDefinitions;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertyKey;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.CJCommunicationsException;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.CJException;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.InvalidConnectionAttributeException;

public class SessionFactory {
   protected ConnectionUrl parseUrl(String url) {
      ConnectionUrl connUrl = ConnectionUrl.getConnectionUrlInstance(url, null);
      if (connUrl != null && (connUrl.getType() == ConnectionUrl.Type.XDEVAPI_SESSION || connUrl.getType() == ConnectionUrl.Type.XDEVAPI_DNS_SRV_SESSION)) {
         return connUrl;
      } else {
         throw (InvalidConnectionAttributeException)ExceptionFactory.createException(
            InvalidConnectionAttributeException.class, "Initialization via URL failed for \"" + url + "\""
         );
      }
   }

   protected Session getSession(ConnectionUrl connUrl) {
      CJException latestException = null;

      for (HostInfo hi : connUrl.getHostsList()) {
         try {
            return new SessionImpl(hi);
         } catch (CJCommunicationsException var7) {
            if (var7.getCause() == null) {
               throw var7;
            }

            latestException = var7;
         }
      }

      if (latestException != null) {
         throw (CJCommunicationsException)ExceptionFactory.createException(
            CJCommunicationsException.class, Messages.getString("Session.Create.Failover.0"), latestException
         );
      } else {
         return null;
      }
   }

   public Session getSession(String url) {
      return this.getSession(this.parseUrl(url));
   }

   public Session getSession(Properties properties) {
      if (properties.containsKey(PropertyKey.xdevapiDnsSrv.getKeyName())
         && (Boolean)PropertyDefinitions.getPropertyDefinition(PropertyKey.xdevapiDnsSrv)
            .parseObject(properties.getProperty(PropertyKey.xdevapiDnsSrv.getKeyName()), null)) {
         ConnectionUrl connUrl = ConnectionUrl.getConnectionUrlInstance(ConnectionUrl.Type.XDEVAPI_DNS_SRV_SESSION.getScheme(), properties);
         return this.getSession(connUrl);
      } else {
         ConnectionUrl connUrl = ConnectionUrl.getConnectionUrlInstance(ConnectionUrl.Type.XDEVAPI_SESSION.getScheme(), properties);
         return new SessionImpl(connUrl.getMainHost());
      }
   }
}
