package com.donutorders.libs.mysql.cj.conf.url;

import com.donutorders.libs.mysql.cj.Messages;
import com.donutorders.libs.mysql.cj.conf.BooleanPropertyDefinition;
import com.donutorders.libs.mysql.cj.conf.ConnectionUrl;
import com.donutorders.libs.mysql.cj.conf.ConnectionUrlParser;
import com.donutorders.libs.mysql.cj.conf.HostInfo;
import com.donutorders.libs.mysql.cj.conf.HostsListView;
import com.donutorders.libs.mysql.cj.conf.PropertyKey;
import com.donutorders.libs.mysql.cj.exceptions.ExceptionFactory;
import com.donutorders.libs.mysql.cj.exceptions.InvalidConnectionAttributeException;
import com.donutorders.libs.mysql.cj.util.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class LoadBalanceDnsSrvConnectionUrl extends ConnectionUrl {
   private static final String DEFAULT_HOST = "";
   private static final int DEFAULT_PORT = -1;

   public LoadBalanceDnsSrvConnectionUrl(ConnectionUrlParser connStrParser, Properties info) {
      super(connStrParser, info);
      this.type = ConnectionUrl.Type.LOADBALANCE_DNS_SRV_CONNECTION;
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
      } else if (hostProps.containsKey(PropertyKey.loadBalanceConnectionGroup.getKeyName())) {
         throw (InvalidConnectionAttributeException)ExceptionFactory.createException(
            InvalidConnectionAttributeException.class,
            Messages.getString("ConnectionString.25", new Object[]{PropertyKey.loadBalanceConnectionGroup.getKeyName()})
         );
      }
   }

   @Override
   protected void injectPerTypeProperties(Map<String, String> props) {
      if (props.containsKey(PropertyKey.loadBalanceAutoCommitStatementThreshold.getKeyName())) {
         try {
            int autoCommitSwapThreshold = Integer.parseInt(props.get(PropertyKey.loadBalanceAutoCommitStatementThreshold.getKeyName()));
            if (autoCommitSwapThreshold > 0) {
               String queryInterceptors = props.get(PropertyKey.queryInterceptors.getKeyName());
               String lbi = "com.donutorders.libs.mysql.cj.jdbc.ha.LoadBalancedAutoCommitInterceptor";
               if (StringUtils.isNullOrEmpty(queryInterceptors)) {
                  props.put(PropertyKey.queryInterceptors.getKeyName(), lbi);
               } else {
                  props.put(PropertyKey.queryInterceptors.getKeyName(), queryInterceptors + "," + lbi);
               }
            }
         } catch (Throwable var5) {
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
   public List<HostInfo> getHostsList(HostsListView view) {
      return this.getHostsListFromDnsSrv(this.getMainHost());
   }
}
