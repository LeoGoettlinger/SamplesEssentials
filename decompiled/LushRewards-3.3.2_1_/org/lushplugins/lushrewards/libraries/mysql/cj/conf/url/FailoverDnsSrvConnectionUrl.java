package org.lushplugins.lushrewards.libraries.mysql.cj.conf.url;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.lushplugins.lushrewards.libraries.mysql.cj.Messages;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.BooleanPropertyDefinition;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.ConnectionUrl;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.ConnectionUrlParser;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.HostInfo;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.HostsListView;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertyKey;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.InvalidConnectionAttributeException;

public class FailoverDnsSrvConnectionUrl extends ConnectionUrl {
   private static final String DEFAULT_HOST = "";
   private static final int DEFAULT_PORT = -1;

   public FailoverDnsSrvConnectionUrl(ConnectionUrlParser connStrParser, Properties info) {
      super(connStrParser, info);
      this.type = ConnectionUrl.Type.FAILOVER_DNS_SRV_CONNECTION;
      HostInfo srvHost = super.getMainHost();
      Map<String, String> hostProps = srvHost.getHostProperties();
      if ("".equals(srvHost.getHost())) {
         throw (InvalidConnectionAttributeException)ExceptionFactory.createException(
            InvalidConnectionAttributeException.class, Messages.getString("ConnectionString.18")
         );
      } else if (this.hosts.size() != 1) {
         throw (InvalidConnectionAttributeException)ExceptionFactory.createException(
            InvalidConnectionAttributeException.class, Messages.getString("ConnectionString.19")
         );
      } else if (srvHost.getPort() != -1) {
         throw (InvalidConnectionAttributeException)ExceptionFactory.createException(
            InvalidConnectionAttributeException.class, Messages.getString("ConnectionString.22")
         );
      } else if (hostProps.containsKey(PropertyKey.dnsSrv.getKeyName())
         && !BooleanPropertyDefinition.booleanFrom(PropertyKey.dnsSrv.getKeyName(), hostProps.get(PropertyKey.dnsSrv.getKeyName()), null)) {
         throw (InvalidConnectionAttributeException)ExceptionFactory.createException(
            InvalidConnectionAttributeException.class, Messages.getString("ConnectionString.23", new Object[]{PropertyKey.dnsSrv.getKeyName()})
         );
      } else if (hostProps.containsKey(PropertyKey.PROTOCOL.getKeyName()) && hostProps.get(PropertyKey.PROTOCOL.getKeyName()).equalsIgnoreCase("PIPE")) {
         throw (InvalidConnectionAttributeException)ExceptionFactory.createException(
            InvalidConnectionAttributeException.class, Messages.getString("ConnectionString.24")
         );
      }
   }

   @Override
   public String getDefaultHost() {
      return "";
   }

   @Override
   public int getDefaultPort() {
      return -1;
   }

   @Override
   public List<HostInfo> getHostsList(HostsListView view) {
      return this.getHostsListFromDnsSrv(this.getMainHost());
   }
}
