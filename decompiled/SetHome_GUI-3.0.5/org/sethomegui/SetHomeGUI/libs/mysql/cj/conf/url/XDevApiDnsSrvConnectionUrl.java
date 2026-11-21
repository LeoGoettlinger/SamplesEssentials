package org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.url;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Messages;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.BooleanPropertyDefinition;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.ConnectionUrl;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.ConnectionUrlParser;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.HostInfo;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.HostsListView;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertyKey;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.InvalidConnectionAttributeException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.util.StringUtils;

public class XDevApiDnsSrvConnectionUrl extends ConnectionUrl {
   private static final String DEFAULT_HOST = "";
   private static final int DEFAULT_PORT = -1;

   public XDevApiDnsSrvConnectionUrl(ConnectionUrlParser connStrParser, Properties info) {
      super(connStrParser, info);
      this.type = ConnectionUrl.Type.XDEVAPI_DNS_SRV_SESSION;
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
      } else if (hostProps.containsKey(PropertyKey.xdevapiDnsSrv.getKeyName())
         && !BooleanPropertyDefinition.booleanFrom(PropertyKey.xdevapiDnsSrv.getKeyName(), hostProps.get(PropertyKey.xdevapiDnsSrv.getKeyName()), null)) {
         throw (InvalidConnectionAttributeException)ExceptionFactory.createException(
            InvalidConnectionAttributeException.class, Messages.getString("ConnectionString.23", new Object[]{PropertyKey.xdevapiDnsSrv.getKeyName()})
         );
      }
   }

   @Override
   protected void preprocessPerTypeHostProperties(Map<String, String> hostProps) {
      if (hostProps.containsKey(PropertyKey.ADDRESS.getKeyName())) {
         String address = hostProps.get(PropertyKey.ADDRESS.getKeyName());
         ConnectionUrlParser.Pair<String, Integer> hostPortPair = ConnectionUrlParser.parseHostPortPair(address);
         String host = StringUtils.safeTrim(hostPortPair.left);
         Integer port = hostPortPair.right;
         if (!StringUtils.isNullOrEmpty(host) && !hostProps.containsKey(PropertyKey.HOST.getKeyName())) {
            hostProps.put(PropertyKey.HOST.getKeyName(), host);
         }

         if (port != -1 && !hostProps.containsKey(PropertyKey.PORT.getKeyName())) {
            hostProps.put(PropertyKey.PORT.getKeyName(), port.toString());
         }
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
   protected void fixProtocolDependencies(Map<String, String> hostProps) {
   }

   @Override
   public List<HostInfo> getHostsList(HostsListView view) {
      return this.getHostsListFromDnsSrv(this.getMainHost());
   }
}
