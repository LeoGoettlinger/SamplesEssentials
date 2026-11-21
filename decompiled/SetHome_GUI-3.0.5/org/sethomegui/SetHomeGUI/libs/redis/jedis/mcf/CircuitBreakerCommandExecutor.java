package org.sethomegui.SetHomeGUI.libs.redis.jedis.mcf;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateSupplier;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandObject;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Connection;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.annots.Experimental;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.executors.CommandExecutor;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.providers.MultiClusterPooledConnectionProvider;

@Experimental
public class CircuitBreakerCommandExecutor extends CircuitBreakerFailoverBase implements CommandExecutor {
   public CircuitBreakerCommandExecutor(MultiClusterPooledConnectionProvider provider) {
      super(provider);
   }

   @Override
   public <T> T executeCommand(CommandObject<T> commandObject) {
      MultiClusterPooledConnectionProvider.Cluster cluster = this.provider.getCluster();
      DecorateSupplier<T> supplier = Decorators.ofSupplier(() -> this.handleExecuteCommand(commandObject, cluster));
      supplier.withRetry(cluster.getRetry());
      supplier.withCircuitBreaker(cluster.getCircuitBreaker());
      supplier.withFallback(this.provider.getFallbackExceptionList(), e -> this.handleClusterFailover(commandObject, cluster.getCircuitBreaker()));
      return (T)supplier.decorate().get();
   }

   private <T> T handleExecuteCommand(CommandObject<T> commandObject, MultiClusterPooledConnectionProvider.Cluster cluster) {
      Object var5;
      try (Connection connection = cluster.getConnection()) {
         var5 = connection.executeCommand(commandObject);
      }

      return (T)var5;
   }

   private <T> T handleClusterFailover(CommandObject<T> commandObject, CircuitBreaker circuitBreaker) {
      this.clusterFailover(circuitBreaker);
      return this.executeCommand(commandObject);
   }
}
