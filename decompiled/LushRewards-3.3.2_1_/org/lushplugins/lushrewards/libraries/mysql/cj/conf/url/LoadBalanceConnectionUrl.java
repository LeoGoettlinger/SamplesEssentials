package org.lushplugins.lushrewards.libraries.mysql.cj.conf.url;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.ConnectionUrl;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.ConnectionUrlParser;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.HostInfo;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertyKey;
import org.lushplugins.lushrewards.libraries.mysql.cj.util.StringUtils;

public class LoadBalanceConnectionUrl extends ConnectionUrl {
   public LoadBalanceConnectionUrl(ConnectionUrlParser connStrParser, Properties info) {
      super(connStrParser, info);
      this.type = ConnectionUrl.Type.LOADBALANCE_CONNECTION;
   }

   public LoadBalanceConnectionUrl(List<HostInfo> hosts, Map<String, String> properties) {
      this.originalConnStr = ConnectionUrl.Type.LOADBALANCE_CONNECTION.getScheme() + "//**internally_generated**" + System.currentTimeMillis() + "**";
      this.originalDatabase = properties.containsKey(PropertyKey.DBNAME.getKeyName()) ? properties.get(PropertyKey.DBNAME.getKeyName()) : "";
      this.type = ConnectionUrl.Type.LOADBALANCE_CONNECTION;
      this.properties.putAll(properties);
      this.injectPerTypeProperties(this.properties);
      this.setupPropertiesTransformer();
      hosts.stream().map(this::fixHostInfo).forEach(this.hosts::add);
   }

   @Override
   protected void injectPerTypeProperties(Map<String, String> props) {
      if (props.containsKey(PropertyKey.loadBalanceAutoCommitStatementThreshold.getKeyName())) {
         try {
            int autoCommitSwapThreshold = Integer.parseInt(props.get(PropertyKey.loadBalanceAutoCommitStatementThreshold.getKeyName()));
            if (autoCommitSwapThreshold > 0) {
               String queryInterceptors = props.get(PropertyKey.queryInterceptors.getKeyName());
               String lbi = "org.lushplugins.lushrewards.libraries.mysql.cj.jdbc.ha.LoadBalancedAutoCommitInterceptor";
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

   public List<String> getHostInfoListAsHostPortPairs() {
      return this.hosts.stream().map(HostInfo::getHostPortPair).collect(Collectors.toList());
   }

   public List<HostInfo> getHostInfoListFromHostPortPairs(Collection<String> hostPortPairs) {
      return hostPortPairs.stream().map(this::getHostOrSpawnIsolated).collect(Collectors.toList());
   }
}
