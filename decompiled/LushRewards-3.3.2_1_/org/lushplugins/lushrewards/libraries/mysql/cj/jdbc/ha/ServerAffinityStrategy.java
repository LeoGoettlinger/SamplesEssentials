package org.lushplugins.lushrewards.libraries.mysql.cj.jdbc.ha;

import java.lang.reflect.InvocationHandler;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.lushplugins.lushrewards.libraries.mysql.cj.jdbc.ConnectionImpl;
import org.lushplugins.lushrewards.libraries.mysql.cj.jdbc.JdbcConnection;
import org.lushplugins.lushrewards.libraries.mysql.cj.util.StringUtils;

public class ServerAffinityStrategy extends RandomBalanceStrategy {
   public String[] affinityOrderedServers = null;

   public ServerAffinityStrategy(String affinityOrdervers) {
      if (!StringUtils.isNullOrEmpty(affinityOrdervers)) {
         this.affinityOrderedServers = affinityOrdervers.split(",");
      }
   }

   @Override
   public ConnectionImpl pickConnection(
      InvocationHandler proxy, List<String> configuredHosts, Map<String, JdbcConnection> liveConnections, long[] responseTimes, int numRetries
   ) throws SQLException {
      if (this.affinityOrderedServers == null) {
         return super.pickConnection(proxy, configuredHosts, liveConnections, responseTimes, numRetries);
      } else {
         Map<String, Long> blockList = ((LoadBalancedConnectionProxy)proxy).getGlobalBlocklist();

         for (String host : this.affinityOrderedServers) {
            if (configuredHosts.contains(host) && !blockList.containsKey(host)) {
               ConnectionImpl conn = (ConnectionImpl)liveConnections.get(host);
               if (conn != null) {
                  return conn;
               }

               try {
                  return ((LoadBalancedConnectionProxy)proxy).createConnectionForHost(host);
               } catch (SQLException var13) {
                  if (((LoadBalancedConnectionProxy)proxy).shouldExceptionTriggerConnectionSwitch(var13)) {
                     ((LoadBalancedConnectionProxy)proxy).addToGlobalBlocklist(host);
                  }
               }
            }
         }

         return super.pickConnection(proxy, configuredHosts, liveConnections, responseTimes, numRetries);
      }
   }
}
